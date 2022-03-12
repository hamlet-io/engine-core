FROM scratch as base

ARG wrapper_file_name=freemarker-wrapper-0.0.0.jar

COPY .hamlet/ .hamlet/
COPY freemarker-wrapper/build/image /image/
COPY freemarker-wrapper/build/libs/${wrapper_file_name} /freemarker-wrapper.jar

FROM scratch as jar

COPY --from=base .hamlet/ .hamlet/
COPY --from=base freemarker-wrapper.jar /freemarker-wrapper.jar

FROM scratch as Darwin

COPY --from=base .hamlet/ .hamlet/
COPY --from=base freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=base image/freemarker-wrapper-Darwin/ image/freemarker-wrapper-Darwin/

FROM scratch as Linux

COPY --from=base .hamlet/ .hamlet/
COPY --from=base freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=base image/freemarker-wrapper-Linux/ image/freemarker-wrapper-Linux/

FROM scratch as Windows

COPY --from=base .hamlet/ .hamlet/
COPY --from=base freemarker-wrapper.jar /freemarker-wrapper.jar
COPY --from=base image/freemarker-wrapper-Windows/ image/freemarker-wrapper-Darwin/
