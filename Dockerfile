FROM artifacts.vdab.be/cops-docker-vdab/cops/base/vdab-java21:2.2.13

COPY bootstrap/target/gebruikersbeheer-derden.war /gebruikersbeheer-derden.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/gebruikersbeheer-derden.war"]