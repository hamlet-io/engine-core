FROM scratch as base_package

ARG wrapper_file_name=freemarker-wrapper-0.0.0.jar

COPY ${wrapper_file_name} /freemarker-wrapper.jar
