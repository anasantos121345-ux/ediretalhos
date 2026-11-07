# Site + API da éDhi Retalhos num único serviço.
# O Spring serve o index.html e o edhi-painel.html, e a API fica em /api no mesmo endereço.

# 1) Compila o backend, já com as páginas do site dentro
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY edhi-backend/ .
COPY index.html edhi-painel.html src/main/resources/static/
RUN chmod +x mvnw && ./mvnw -q -B -DskipTests package

# 2) Imagem final, só com o Java e o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/painel-0.0.1-SNAPSHOT.jar app.jar
# O plano grátis do Render tem 512 MB de memória
ENV JAVA_OPTS="-Xmx300m -XX:+UseSerialGC"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
