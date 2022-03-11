FROM scratch as full_package

ARG wrapper_file_name=freemarker-wrapper-0.0.0.jar

COPY .hamlet/ .hamlet/
COPY freemarker-wrapper/build/image /image/
COPY freemarker-wrapper/build/libs/${wrapper_file_name} /freemarker-wrapper.jar

FROM scratch as jar

COPY .hamlet/ .hamlet/
COPY freemarker-wrapper/build/libs/${wrapper_file_name} /freemarker-wrapper.jar

FROM scratch as Darwin

COPY --from=full_package .hamlet/ .hamlet/
COPY --from=full_package freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=full_package image/freemarker-wrapper-Darwin/ image/freemarker-wrapper-Darwin/

FROM scratch as Linux

COPY --from=full_package .hamlet/ .hamlet/
COPY --from=full_package freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=full_package image/freemarker-wrapper-Linux/ image/freemarker-wrapper-Linux/

FROM scratch as Windows

COPY --from=full_package .hamlet/ .hamlet/
COPY --from=full_package freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=full_package image/freemarker-wrapper-Windows/ image/freemarker-wrapper-Darwin/
