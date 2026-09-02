package io.github.lijiajia3515.cairo.web.error;

import io.github.lijiajia3515.cairo.core.business.*;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.web.DefaultWebError;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.*;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CairoErrorAttributes implements ErrorAttributes, HandlerExceptionResolver, Ordered {
    private static final String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";
    public static final String ERROR_REQUEST_ID = "requestId";

    public static final String ERROR_TIME = "time";
    public static final String ERROR_STATUS = "status";
    public static final String ERROR_ERROR = "error";
    public static final String ERROR_PATH = "path";
    public static final String ERROR_MESSAGE = "message";
    public static final String ERROR_ERRORS = "errors";
    public static final String ERROR_TRACE = "trace";
    public static final String ERROR_EXCEPTION = "exception";

    public static final String BUSINESS = "business";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE));

        if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
            errorAttributes.remove(ERROR_EXCEPTION);
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            errorAttributes.remove(ERROR_TRACE);
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get(ERROR_MESSAGE) != null) {
            errorAttributes.remove(ERROR_MESSAGE);
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
            errorAttributes.remove(ERROR_ERRORS);
        }

        Business business = getBusiness(request);
        return Collections.singletonMap(BUSINESS, businessResult(business, errorAttributes));
    }


    @Override
    public Throwable getError(WebRequest webRequest) {
        Throwable exception = getAttribute(webRequest, ERROR_INTERNAL_ATTRIBUTE);
        if (exception == null) {
            exception = getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
        }
        return exception;
    }


    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        storeErrorAttributes(request, ex);
        return null;
    }

    private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
        request.setAttribute(ERROR_INTERNAL_ATTRIBUTE, ex);
    }

    private Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
        Throwable error = getError(request);
        MergedAnnotation<ResponseStatus> responseStatusAnnotation = null;
        if (error != null) {
            responseStatusAnnotation = MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
        }

        Map<String, Object> errorAttributes = new LinkedHashMap<>();

        errorAttributes.put(ERROR_REQUEST_ID, request.getHeader("X-Trace-Id"));
        errorAttributes.put(ERROR_TIME, LocalDateTime.now());
        errorAttributes.put(ERROR_PATH, getAttribute(request, RequestDispatcher.ERROR_REQUEST_URI));
        HttpStatus errorStatus = determineHttpStatus(request, error, responseStatusAnnotation);
        errorAttributes.put(ERROR_STATUS, errorStatus.value());
        errorAttributes.put(ERROR_ERROR, errorStatus.getReasonPhrase());

        errorAttributes.put(ERROR_MESSAGE, determineMessage(request, getException(request), responseStatusAnnotation));
        handleException(errorAttributes, determineException(error), includeStackTrace);

        return errorAttributes;
    }

    public Business getBusiness(WebRequest request) {
        Throwable error = getError(request);
        return determineBusiness(error);
    }

    private HttpStatus determineHttpStatus(RequestAttributes requestAttributes, Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof AuthenticationServiceException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        Integer status = getAttribute(requestAttributes, RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            return HttpStatus.valueOf(status);
        }

        if (error instanceof ResponseStatusException) {

            return HttpStatus.valueOf(((ResponseStatusException) error).getStatusCode().value());
        }

        return Optional.ofNullable(responseStatusAnnotation)
                .flatMap(x -> x.getValue("code", HttpStatus.class))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Business determineBusiness(Throwable throwable) {
        String businessCode = null;
        String businessMessage = null;
        // business
        if (throwable instanceof BusinessException) {
            businessCode = ((BusinessException) throwable).getBusiness().getCode();
            businessMessage = ((BusinessException) throwable).getBusiness().getMessage();
        }

        // servlet
        if (throwable instanceof ServletException) {
            if (throwable instanceof NoHandlerFoundException) {
                businessCode = RequestBusiness.NOT_FOUND.getCode();
                businessMessage = RequestBusiness.NOT_FOUND.getMessage() + "(" + throwable.getMessage() + ")";
            }
            if (throwable instanceof HttpRequestMethodNotSupportedException) {
                businessCode = RequestBusiness.NOT_FOUND.getCode();
                businessMessage = RequestBusiness.NOT_FOUND.getMessage() + "(" + throwable.getMessage() + ")";
            }
            if (throwable instanceof ServletRequestBindingException || throwable instanceof MissingServletRequestPartException) {
                businessCode = ParamsBusiness.VALIDATION_FAILED.getCode();
                businessMessage = ParamsBusiness.VALIDATION_FAILED.getMessage() + "(" + throwable.getMessage() + ")";
            }
            if (throwable instanceof HttpMediaTypeException) {
                if (throwable instanceof HttpMediaTypeNotAcceptableException) {
                    businessCode = RequestBusiness.NOT_ACCEPTED.getCode();
                    businessMessage = RequestBusiness.NOT_ACCEPTED.getMessage() + "(" + throwable.getMessage() + ")";
                }
                if (throwable instanceof HttpMediaTypeNotSupportedException) {
                    businessCode = RequestBusiness.NOT_SUPPORTED.getCode();
                    businessMessage = RequestBusiness.NOT_SUPPORTED.getMessage() + "(" + throwable.getMessage() + ")";
                }
            }
        }

        // server
        if (throwable instanceof ResponseStatusException) {
            if (throwable instanceof MethodNotAllowedException) {
                businessCode = RequestBusiness.NOT_FOUND.getCode();
                businessMessage = RequestBusiness.NOT_FOUND.getMessage() + "(" + throwable.getMessage() + ")";
            } else if (throwable instanceof ServerWebInputException) {
                businessCode = ParamsBusiness.ERROR.getCode();
                businessMessage = ParamsBusiness.ERROR.getMessage() + "(" + throwable.getMessage() + ")";
            } else if (throwable instanceof UnsupportedMediaTypeStatusException) {
                businessCode = RequestBusiness.NOT_ACCEPTED.getCode();
                businessMessage = RequestBusiness.NOT_ACCEPTED.getMessage() + "(" + throwable.getMessage() + ")";
            } else if (throwable instanceof NotAcceptableStatusException) {
                businessCode = RequestBusiness.NOT_SUPPORTED.getCode();
                businessMessage = RequestBusiness.NOT_SUPPORTED.getMessage() + "(" + throwable.getMessage() + ")";
            } else if (throwable instanceof ServerErrorException) {
                businessCode = ServiceBusiness.ERROR.getCode();
                businessMessage = ServiceBusiness.ERROR.getMessage() + "(" + throwable.getMessage() + ")";
            }
        }

        // unknown host
        if (throwable instanceof UnknownHostException) {
            businessCode = ServiceBusiness.UNAVAILABLE.getCode();
            businessMessage = ServiceBusiness.UNAVAILABLE.getMessage() + "(" + throwable.getMessage() + ")";
        }

        // 认证服务异常
        if (throwable instanceof AuthenticationServiceException) {
            businessCode = AuthBusiness.ERROR.getCode();
            businessMessage = AuthBusiness.ERROR.getMessage() + "(" + throwable.getMessage() + ")";
        }
        String finalBusinessCode = Optional.ofNullable(businessCode).orElse(ServiceBusiness.ERROR.getCode());
        String finalBusinessMessage = Optional.ofNullable(businessMessage).orElse(ServiceBusiness.ERROR.getMessage() + "(" + throwable.getMessage() + ")");
        return new Business() {
            @Override
            public String code() {
                return finalBusinessCode;
            }

            @Override
            public String message() {
                return finalBusinessMessage;
            }
        };
    }

    private String determineMessage(WebRequest request, Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        Object message = getAttribute(request, RequestDispatcher.ERROR_MESSAGE);
        if (!ObjectUtils.isEmpty(message)) {
            return message.toString();
        }
        if (error != null) {
            if (error instanceof BusinessException) {
                return error.getMessage();
            }
            if (error instanceof BindingResult) {
                return error.getMessage();
            }
            if (error instanceof ResponseStatusException) {
                return ((ResponseStatusException) error).getReason();
            }
        }


        return Optional.ofNullable(responseStatusAnnotation)
                .flatMap(x -> x.getValue("reason", String.class))
                .or(() -> Optional.ofNullable(error).map(Throwable::getMessage))
                .orElse("");
    }

    private Throwable determineException(Throwable error) {
        Throwable self = error;
        while (self instanceof ServletException && self.getCause() != null) {
            self = self.getCause();
        }
        if (self instanceof ResponseStatusException) {
            return (self.getCause() != null) ? self.getCause() : self;
        }
        return self;
    }

    private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
        if (error != null) {
            StringWriter stackTrace = new StringWriter();
            error.printStackTrace(new PrintWriter(stackTrace));
            stackTrace.flush();
            errorAttributes.put(ERROR_TRACE, stackTrace.toString());
        }
    }

    private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
        if (error != null) {
            errorAttributes.put(ERROR_EXCEPTION, error.getClass().getName());
            if (error instanceof BindingResult) {
                BindingResult result = (BindingResult) error;
                if (result.hasErrors()) {
                    errorAttributes.put(ERROR_ERRORS, result.getAllErrors());
                }
            }
        }

        if (includeStackTrace) {
            addStackTrace(errorAttributes, error);
        }

    }

    private BusinessResult<DefaultWebError> businessResult(Business business, Map<String, Object> errorAttribute) {
        return BusinessResult.<DefaultWebError>builder()
                .code(business.getCode())
                .message(business.getMessage())
                .data(DefaultWebError.builder()
                        .requestId((String) errorAttribute.get(ERROR_REQUEST_ID))
                        .time((LocalDateTime) errorAttribute.get(ERROR_TIME))
                        .path((String) errorAttribute.get(ERROR_PATH))
                        .status((int) errorAttribute.get(ERROR_STATUS))
                        .error((String) errorAttribute.get(ERROR_ERROR))
                        .message((String) errorAttribute.get(ERROR_MESSAGE))
                        .trace((String) errorAttribute.get(ERROR_TRACE))
                        .exception((String) errorAttribute.get(ERROR_EXCEPTION))
                        .errors(errorAttribute.get(ERROR_ERRORS))
                        .build())
                .build();
    }

    private Throwable getException(WebRequest webRequest) {
        Throwable error = getError(webRequest);
        if (error != null) {
            while (error instanceof ServletException && error.getCause() != null) {
                error = error.getCause();
            }
            return error;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
}
