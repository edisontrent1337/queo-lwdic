FROM openjdk:jre-alpine
ARG JAR_FILE
COPY ./${JAR_FILE} app.jar
COPY ./musl/ld-musl-x86_64.path /etc/ld-musl-x86_64.path
ADD target/lib /lib
ENTRYPOINT ["java","-jar","/app.jar"]
