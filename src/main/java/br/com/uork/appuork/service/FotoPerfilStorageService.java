package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.usuario.FotoUploadRequestDTO;
import br.com.uork.appuork.dto.usuario.FotoUploadResponseDTO;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FotoPerfilStorageService {

    private static final Map<String, String> EXTENSOES_PERMITIDAS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final UsuarioRepository usuarioRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String publicBaseUrl;
    private final Duration uploadExpiration;
    private final Duration downloadExpiration;
    private final long maxFileSizeBytes;

    public FotoPerfilStorageService(
            UsuarioRepository usuarioRepository,
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${uork.storage.s3.bucket}") String bucket,
            @Value("${uork.storage.s3.public-base-url:}") String publicBaseUrl,
            @Value("${uork.storage.s3.upload-expiration-minutes:5}") long uploadExpirationMinutes,
            @Value("${uork.storage.s3.download-expiration-minutes:1440}") long downloadExpirationMinutes,
            @Value("${uork.storage.s3.max-file-size-bytes:5242880}") long maxFileSizeBytes) {
        this.usuarioRepository = usuarioRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket == null ? "" : bucket.trim();
        this.publicBaseUrl = removerBarraFinal(publicBaseUrl);
        this.uploadExpiration = Duration.ofMinutes(uploadExpirationMinutes);
        this.downloadExpiration = Duration.ofMinutes(downloadExpirationMinutes);
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public FotoUploadResponseDTO gerarUrlUpload(String email, FotoUploadRequestDTO dto) {
        validarConfiguracao();
        if (dto == null) {
            throw new IllegalArgumentException("Dados da foto são obrigatórios");
        }

        String contentType = normalizarContentType(dto.contentType());
        String extensao = EXTENSOES_PERMITIDAS.get(contentType);
        if (extensao == null) {
            throw new IllegalArgumentException("Use uma imagem JPEG, PNG ou WebP");
        }

        if (dto.tamanhoBytes() == null || dto.tamanhoBytes() <= 0) {
            throw new IllegalArgumentException("O tamanho da imagem é inválido");
        }
        if (dto.tamanhoBytes() > maxFileSizeBytes) {
            throw new IllegalArgumentException("A imagem deve ter no máximo 5 MB");
        }

        Usuario usuario = buscarUsuario(email);
        String objectKey = prefixoUsuario(usuario.getId())
                + UUID.randomUUID() + "." + extensao;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        var presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(uploadExpiration)
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        return new FotoUploadResponseDTO(
                presigned.url().toString(),
                objectKey,
                Instant.now().plus(uploadExpiration)
        );
    }

    @Transactional
    public void confirmarUpload(String email, String objectKey) {
        validarConfiguracao();
        Usuario usuario = buscarUsuario(email);
        validarChaveDoUsuario(usuario, objectKey);

        HeadObjectResponse objeto = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .build()
        );

        String contentType = normalizarContentType(objeto.contentType());
        if (!EXTENSOES_PERMITIDAS.containsKey(contentType)) {
            excluirObjetoSilenciosamente(objectKey);
            throw new IllegalArgumentException("O arquivo enviado não é uma imagem permitida");
        }
        if (objeto.contentLength() == null || objeto.contentLength() <= 0
                || objeto.contentLength() > maxFileSizeBytes) {
            excluirObjetoSilenciosamente(objectKey);
            throw new IllegalArgumentException("A imagem enviada possui tamanho inválido");
        }

        String fotoAnterior = usuario.getFotoPerfilKey();
        usuario.setFotoPerfilKey(objectKey);
        usuarioRepository.save(usuario);

        if (fotoAnterior != null && !fotoAnterior.equals(objectKey)) {
            excluirObjetoSilenciosamente(fotoAnterior);
        }
    }

    @Transactional
    public void removerFoto(String email) {
        validarConfiguracao();
        Usuario usuario = buscarUsuario(email);
        String objectKey = usuario.getFotoPerfilKey();
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        usuario.setFotoPerfilKey(null);
        usuarioRepository.save(usuario);
        excluirObjeto(objectKey);
    }

    public String gerarUrlLeitura(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        validarConfiguracao();

        if (!publicBaseUrl.isBlank()) {
            return publicBaseUrl + "/" + objectKey;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        return s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(downloadExpiration)
                        .getObjectRequest(getObjectRequest)
                        .build()
        ).url().toString();
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void validarChaveDoUsuario(Usuario usuario, String objectKey) {
        if (objectKey == null || objectKey.isBlank()
                || !objectKey.startsWith(prefixoUsuario(usuario.getId()))) {
            throw new IllegalArgumentException("A foto informada não pertence ao usuário autenticado");
        }
    }

    private String prefixoUsuario(Long usuarioId) {
        return "usuarios/" + usuarioId + "/perfil/";
    }

    private void excluirObjeto(String objectKey) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .build()
        );
    }

    private void excluirObjetoSilenciosamente(String objectKey) {
        try {
            excluirObjeto(objectKey);
        } catch (RuntimeException ignored) {
            // A foto nova já está válida; uma falha ao limpar o objeto anterior
            // não deve desfazer a atualização do perfil.
        }
    }

    private void validarConfiguracao() {
        if (bucket.isBlank()) {
            throw new IllegalStateException(
                    "O bucket de fotos não está configurado em AWS_S3_PROFILE_BUCKET"
            );
        }
    }

    private String normalizarContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private static String removerBarraFinal(String url) {
        if (url == null) {
            return "";
        }
        return url.trim().replaceAll("/+$", "");
    }
}
