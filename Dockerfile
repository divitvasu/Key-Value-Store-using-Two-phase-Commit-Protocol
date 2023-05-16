#BASE IMAGE for server
FROM bellsoft/liberica-openjdk-alpine-musl:17 AS server-build
COPY . /docker
WORKDIR /docker
RUN javac com/distributedsystems/Server/*.java
CMD ["java", "com.distributedsystems.Server.Server_app"]

#BASE IMAGE for client
FROM bellsoft/liberica-openjdk-alpine-musl:17 AS client-build
COPY . /docker
WORKDIR /docker
RUN javac com/distributedsystems/Client1_app.java
CMD ["java", "com.distributedsystems.Client1_app"]


#BASE IMAGE for coordinator
FROM bellsoft/liberica-openjdk-alpine-musl:17 AS coordinator-build
COPY . /docker
WORKDIR /docker
RUN javac com/distributedsystems/Coordinator/*.java
CMD ["java", "com.distributedsystems.Coordinator.Coordinator"]
