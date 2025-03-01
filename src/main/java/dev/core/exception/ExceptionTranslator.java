package dev.core.exception;

import dev.account.web.errors.EmailAlreadyUsedException;
import dev.account.web.errors.InvalidPasswordException;
import dev.account.web.errors.MobileNumberAlreadyUsedException;
import dev.core.config.FoodyConstants;
import dev.core.exception.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;
import dev.core.utils.HeaderUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.*;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (<a href="https://tools.ietf.org/html/rfc7807">...</a>).
 */
@ControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final boolean CASUAL_CHAIN_ENABLED = false;

    @Value("${foody.clientApp.name}")
    private String applicationName;

    private final Environment env;

    public ExceptionTranslator(Environment env) {
        this.env = env;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleAnyException(Throwable ex, NativeWebRequest request) {
        ProblemDetailWithCause pdCause = wrapAndCustomizeProblem(ex, request);
        return handleExceptionInternal((Exception) ex, pdCause, buildHeaders(ex),
                HttpStatusCode.valueOf(pdCause.getStatus()), request);
    }

    @Nullable
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request
    ) {
        body = body == null ? wrapAndCustomizeProblem(ex, (NativeWebRequest) request) : body;
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    protected ProblemDetailWithCause wrapAndCustomizeProblem(Throwable ex, NativeWebRequest request) {
        return customizeProblem(getProblemDetailWithCause(ex), ex, request);
    }

    private ProblemDetailWithCause getProblemDetailWithCause(Throwable ex) {
        if (ex instanceof InvalidPasswordException) return (ProblemDetailWithCause) new InvalidPasswordException()
                .getBody();
        if (ex instanceof EmailAlreadyUsedException) return (ProblemDetailWithCause) new EmailAlreadyUsedException()
                .getBody();
        if (ex instanceof MobileNumberAlreadyUsedException) return (ProblemDetailWithCause) new MobileNumberAlreadyUsedException()
                .getBody();
        if (ex instanceof GenericApiException) return (ProblemDetailWithCause) new GenericApiException()
                .getBody();
        if (ex instanceof ErrorResponseException exp && exp.getBody() instanceof ProblemDetailWithCause problemDetailWithCause
        ) return problemDetailWithCause;
        return ProblemDetailWithCauseBuilder.instance().withStatus(toStatus(ex).value()).build();
    }

    protected ProblemDetailWithCause customizeProblem(ProblemDetailWithCause problem, Throwable err, NativeWebRequest request) {
        if (problem.getStatus() <= 0) problem.setStatus(resolveHttpStatus(err));

        if (problem.getType().equals(URI.create("about:blank"))) {
            problem.setType(getMappedType(err));
        }

        String title = extractTitle(err, problem.getStatus());
        String problemTitle = problem.getTitle();
        if (problemTitle == null || !problemTitle.equals(title)) {
            problem.setTitle(title);
        }

        String detail = getSimplifiedDetail(err);
        if (detail != null) {
            problem.setDetail(detail);
        }

        Map<String, Object> problemProperties = problem.getProperties();
        if (problemProperties == null || !problemProperties.containsKey(MESSAGE_KEY)) {
            problem.setProperty(
                    MESSAGE_KEY,
                    getMappedMessageKey(err) != null ? getMappedMessageKey(err) : "error.http." + problem.getStatus()
            );
        }

        if (problemProperties == null || !problemProperties.containsKey(PATH_KEY)) {
            problem.setProperty(PATH_KEY, extractURI(request));
        }

        problem.setInstance(null);

        if ((err instanceof MethodArgumentNotValidException fieldException) &&
                (problemProperties == null || !problemProperties.containsKey(FIELD_ERRORS_KEY))
        ) {
            problem.setProperty(FIELD_ERRORS_KEY, getFieldErrors(fieldException));
        }

        if ((err instanceof ConstraintViolationException violationException) &&
                (problemProperties == null || !problemProperties.containsKey(FIELD_ERRORS_KEY))
        ) {
            problem.setProperty(FIELD_ERRORS_KEY, getConstraintViolationErrors(violationException));
        }

        problem.setCause(buildCause(err.getCause(), request).orElse(null));

        return problem;
    }

    private String extractTitle(Throwable err, int statusCode) {
        return getCustomizedTitle(err) != null ? getCustomizedTitle(err) : extractTitleForResponseStatus(err, statusCode);
    }

    private List<FieldErrorVM> getFieldErrors(MethodArgumentNotValidException ex) {
        return ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f ->
                        new FieldErrorVM(
                                f.getObjectName().replaceFirst("DTO$", ""),
                                f.getField(),
                                StringUtils.isNotBlank(f.getDefaultMessage()) ? f.getDefaultMessage() : f.getCode()
                        )
                )
                .toList();
    }

    private List<FieldErrorVM> getConstraintViolationErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    String objectName = path.contains(".") ?
                            path.substring(0, path.lastIndexOf('.')) :
                            violation.getRootBeanClass().getSimpleName();

                    return new FieldErrorVM(
                            objectName,
                            field,
                            violation.getMessage()
                    );
                })
                .toList();
    }

    private String extractTitleForResponseStatus(Throwable err, int statusCode) {
        ResponseStatus specialStatus = extractResponseStatus(err);
        return specialStatus == null ? HttpStatus.valueOf(statusCode).getReasonPhrase() : specialStatus.reason();
    }

    private String extractURI(NativeWebRequest request) {
        HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);
        return nativeRequest != null ? nativeRequest.getRequestURI() : StringUtils.EMPTY;
    }

    private HttpStatus toStatus(final Throwable throwable) {
        // Let the ErrorResponse take this responsibility
        if (throwable instanceof ErrorResponse err) return HttpStatus.valueOf(err.getBody().getStatus());

        return (HttpStatus) Optional.ofNullable(getMappedStatus(throwable)).orElse(
                Optional.ofNullable(resolveResponseStatus(throwable)).map(ResponseStatus::value).orElse(HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    private int resolveHttpStatus(Throwable ex) {
        if (ex instanceof GenericApiException genericApiException) {
            return (genericApiException.getErrorCode().getHttpStatus().value());
        }
        if (ex instanceof ErrorResponseException) return HttpStatus.BAD_REQUEST.value();
        if (ex instanceof AccessDeniedException) return HttpStatus.FORBIDDEN.value();
        if (ex instanceof BadCredentialsException) return HttpStatus.UNAUTHORIZED.value();
        if (ex instanceof MethodArgumentNotValidException) return HttpStatus.BAD_REQUEST.value();
        if (ex instanceof ConstraintViolationException) return HttpStatus.BAD_REQUEST.value();
        if (ex.getCause() instanceof ConstraintViolationException) return HttpStatus.BAD_REQUEST.value();
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private ResponseStatus extractResponseStatus(final Throwable throwable) {
        return resolveResponseStatus(throwable);
    }

    private ResponseStatus resolveResponseStatus(final Throwable type) {
        final ResponseStatus candidate = findMergedAnnotation(type.getClass(), ResponseStatus.class);
        return candidate == null && type.getCause() != null ? resolveResponseStatus(type.getCause()) : candidate;
    }

    private URI getMappedType(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) return ErrorConstants.CONSTRAINT_VIOLATION_TYPE;
        if (err instanceof ConstraintViolationException) return ErrorConstants.CONSTRAINT_VIOLATION_TYPE;
        if (err instanceof GenericApiException genericApiException) return genericApiException.getBody().getType();
        return ErrorConstants.DEFAULT_TYPE;
    }

    private String getMappedMessageKey(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) {
            return ErrorConstants.ERR_VALIDATION;
        } else if (err instanceof ConstraintViolationException) {
            return ErrorConstants.ERR_VALIDATION;
        } else if (err instanceof ConcurrencyFailureException || err.getCause() instanceof ConcurrencyFailureException) {
            return ErrorConstants.ERR_CONCURRENCY_FAILURE;
        } else if (err instanceof GenericApiException genericApiException) {
            return genericApiException.getErrorCode().getMessage();
        }
        return null;
    }

    private String getCustomizedTitle(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) return "Validation Error";
        if (err instanceof ConstraintViolationException) return "Validation Error";
        if (err instanceof GenericApiException genericApiException)
            return genericApiException.getErrorCode().getHttpStatus().getReasonPhrase();
        return null;
    }

    private String getSimplifiedDetail(Throwable err) {
        if (err instanceof MethodArgumentNotValidException || err instanceof ConstraintViolationException) {
            return "One or more fields failed validation. Please check the 'fieldErrors' section for details.";
        }

        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(FoodyConstants.SPRING_PROFILE_PRODUCTION)) {
            if (err instanceof HttpMessageConversionException) return "Unable to process the request format";
            if (err instanceof DataAccessException) return "A database error occurred";
            if (err instanceof GenericApiException genericApiException) return genericApiException.getBody().getDetail();
            if (containsPackageName(err.getMessage())) return "An unexpected server error occurred";
        }

        if (err instanceof GenericApiException genericApiException) return genericApiException.getBody().getDetail();

        String message = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
        if (message != null && message.length() > 100) {
            return message.substring(0, 97) + "...";
        }
        return message;
    }

    private Object getMappedStatus(Throwable err) {
        if (err instanceof AccessDeniedException) return HttpStatus.FORBIDDEN;
        if (err instanceof ConcurrencyFailureException) return HttpStatus.CONFLICT;
        if (err instanceof BadCredentialsException) return HttpStatus.UNAUTHORIZED;
        if (err instanceof MethodArgumentNotValidException) return HttpStatus.BAD_REQUEST;
        if (err instanceof ConstraintViolationException) return HttpStatus.BAD_REQUEST;
        if (err.getCause() instanceof ConstraintViolationException) return HttpStatus.BAD_REQUEST;
        if (err instanceof GenericApiException genericApiException) return genericApiException.getErrorCode().getHttpStatus().value();
        return null;
    }

    private HttpHeaders buildHeaders(Throwable err) {
        return err instanceof BadRequestAlertException badRequestAlertException
                ? HeaderUtils.createFailureAlert(
                applicationName,
                true,
                badRequestAlertException.getEntityName(),
                badRequestAlertException.getErrorKey(),
                badRequestAlertException.getMessage()
        )
                : null;
    }

    public Optional<ProblemDetailWithCause> buildCause(final Throwable throwable, NativeWebRequest request) {
        if (throwable != null && isCasualChainEnabled()) {
            return Optional.of(customizeProblem(getProblemDetailWithCause(throwable), throwable, request));
        }
        return Optional.empty();
    }

    private boolean isCasualChainEnabled() {
        return CASUAL_CHAIN_ENABLED;
    }

    private boolean containsPackageName(String message) {
        // This list is for sure not complete
        return StringUtils.containsAny(message, "org.", "java.", "net.", "jakarta.", "javax.", "com.", "io.", "de.", "dev.venda");
    }
}