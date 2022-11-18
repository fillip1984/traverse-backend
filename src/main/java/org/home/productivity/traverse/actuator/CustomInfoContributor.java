package org.home.productivity.traverse.actuator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.home.productivity.traverse.commons.StringUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Customize Spring Actuator /info endpoint with additional info.
 * <p>
 * Most of the customization of info should be done via application.properties.
 * Check documentation before customizing here
 */
@Component
@Slf4j
public class CustomInfoContributor implements InfoContributor {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Value("${application.actuator.bean.packages.to.include}")
    private String[] beanPackagesToInclude;

    @Override
    public void contribute(Builder builder) {
        try {
            log.debug("Adding customized info to InfoContributor");
            builder.withDetail("profile", getProfile())
                    .withDetail("mappings", getMappings())
                    .withDetail("beans", getBeans());
            log.trace("Added customized info to InfoContributor");
        } catch (Exception e) {
            var msg = "Exception occurred while adding customized info to InfoContributor";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Build out active profiles Map for InfoContributor
     * 
     * @return
     */
    private Map<String, String> getProfile() {
        try {
            log.debug("Building profile information");
            Map<String, String> info = new HashMap<>();
            info.put("activeProfiles", Arrays.toString(applicationContext.getEnvironment().getActiveProfiles()));
            log.trace("Built profile information: {}", info);
            return info;
        } catch (Exception e) {
            var msg = "Exception occurred while building profile information";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Build out url end point mappings Map for InfoContributor
     * 
     * @return
     */
    private Map<String, MappingDTO> getMappings() {
        try {
            log.debug("Building out mappings");
            Map<String, MappingDTO> mappings = new TreeMap<>();

            var requestMappingHandlerMapping = applicationContext.getBean("requestMappingHandlerMapping",
                    RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
            map.forEach((requestMappingInfo, handlerMethod) -> {
                requestMappingInfo.getMethodsCondition().getMethods().forEach(method -> {
                    requestMappingInfo.getPathPatternsCondition().getPatternValues().forEach(path -> {
                        var controllerShortName = (String) handlerMethod.getBean();
                        var controller = (String) handlerMethod.getBeanType().getName();
                        var mappingDTO = mappings.getOrDefault(controllerShortName,
                                new MappingDTO(controller, new ArrayList<RequestMappingDTO>()));
                        mappingDTO.getRequestMappings()
                                .add(new RequestMappingDTO(method, path, handlerMethod.toString()));
                        Collections.sort(mappingDTO.getRequestMappings());
                        mappings.put(controllerShortName, mappingDTO);
                    });
                });
            });
            log.trace("Built out mappings: {}", mappings);
            return mappings;
        } catch (Exception e) {
            var msg = "Exception occurred while building out mappings";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        // list out controller/restcontroller endpoints

    }

    /**
     * Build out beans Map for InfoContributor
     * <P>
     * Unlike the out of box Spring Actuator /beans end point this list of beans can
     * be filtered by package name to show only beans of interest. See
     * application.properties -> actuator.bean.packages.to.include
     * 
     * @return
     */
    private Map<String, BeanDTO> getBeans() {
        try {
            log.debug("Building beans information");
            // TODO; rewrite with lambdas, logic is complex looking and doesn't need to be
            Map<String, BeanDTO> beans = new TreeMap<>();
            log.debug("Building beans DTO for spring actuator /info endpoint");
            var beanFactory = applicationContext.getBeanFactory();
            var sortedBeanNames = applicationContext.getBeanDefinitionNames();
            Arrays.sort(sortedBeanNames);
            for (var beanName : sortedBeanNames) {
                var beanDefinition = beanFactory.getBeanDefinition(beanName);

                // filter down to classes of interest
                if (StringUtilities.startsWithIn(
                        beanFactory.getType(beanName).getPackageName(), beanPackagesToInclude)) {

                    var beanDTO = new BeanDTO(beanFactory.getAliases(beanName), beanDefinition.getScope(),
                            beanFactory.getType(beanName),
                            beanDefinition.getResourceDescription(), beanFactory.getDependenciesForBean(beanName),
                            getValueAnnotatedFieldsForBean(beanName));
                    log.trace("Generated beanDTO: {}", beanDTO);
                    beans.put(beanName, beanDTO);

                }
            }
            log.trace("Built beans information: {}", beans);
            return beans;
        } catch (Exception e) {
            var msg = "Exception occurred while building out beans";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Given bean name, retrieve @Value annotation information for all annotated
     * fields
     * <p>
     * Build DTO that contains field name and @Value annotation value (what logic or
     * property is used to inject a value into the field)
     * 
     * @param beanName
     * @return
     */
    private ValueAnnotatedField[] getValueAnnotatedFieldsForBean(String beanName) {
        try {
            log.debug("Retrieving @Value annotation info for bean name: {}", beanName);
            var bean = applicationContext.getBeanFactory().getBean(beanName);
            var beanFields = bean.getClass().getDeclaredFields();
            var fieldsWithAnnotation = Arrays.stream(beanFields)
                    .filter(field -> field.isAnnotationPresent(Value.class))
                    .collect(Collectors.toSet());
            var valueAnnotatedFields = fieldsWithAnnotation.stream()
                    .map(aField -> {
                        var annotation = aField.getAnnotation(Value.class);
                        var value = annotation.value();
                        var fieldName = aField.getName();
                        return new ValueAnnotatedField(fieldName, value);
                    })
                    .toArray(ValueAnnotatedField[]::new);
            log.debug("Retrieved @Value annotation info for bean name: {}. @Value fields include: {}", beanName,
                    Arrays.toString(valueAnnotatedFields));
            return valueAnnotatedFields;
        } catch (Exception e) {
            var msg = "Exception occurred while attempting to retrieve @Value annotation info for bean name: "
                    + beanName;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Data
    public static final class MappingDTO {
        private final String controller;
        private List<RequestMappingDTO> requestMappings;

        private MappingDTO(String controller, List<RequestMappingDTO> requestMappings) {
            this.controller = controller;
            this.requestMappings = requestMappings;
        }
    }

    @Data
    public static final class RequestMappingDTO implements Comparable<RequestMappingDTO> {
        private final RequestMethod method;
        private final String path;
        private final String handler;

        private RequestMappingDTO(RequestMethod method, String path, String handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }

        @Override
        public int compareTo(RequestMappingDTO otherRequestMappingDTO) {
            int compare = this.method.compareTo(otherRequestMappingDTO.getMethod());
            if (compare == 0) {
                compare = this.path.compareTo(otherRequestMappingDTO.getPath());
            }
            return compare;
        }
    }

    /**
     * A description of a bean in an application context, primarily intended for
     * serialization to JSON.
     * <p>
     * Borrowed from springboot source code, because its constructor is private:
     * https://github.com/spring-projects/spring-boot/blob/d354c03e6398f49fb125315956dcf7da9afd6142/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/beans/BeansEndpoint.java#L139
     */
    @Data
    public static final class BeanDTO {
        private final String[] aliases;
        private final String scope;
        private final Class<?> type;
        private final String resource;
        private final String[] dependencies;
        // custom, not from Spring code
        private final ValueAnnotatedField[] valueAnnotatedFields;

        private BeanDTO(String[] aliases, String scope, Class<?> type, String resource, String[] dependencies,
                ValueAnnotatedField[] valueAnnotatedFields) {
            this.aliases = aliases;
            this.scope = (StringUtils.isNotBlank(scope) ? scope : BeanDefinition.SCOPE_SINGLETON);
            this.type = type;
            this.resource = resource;
            this.dependencies = dependencies;
            this.valueAnnotatedFields = valueAnnotatedFields;
        }
    }

    /**
     * A description of a @Value annotated field. Primarily intended for
     * serializaion to JSON within BeanDTO
     */
    @Data
    public static final class ValueAnnotatedField {
        private final String field;
        private final String value;

        private ValueAnnotatedField(String field, String value) {
            this.field = field;
            this.value = value;
        }
    }

}