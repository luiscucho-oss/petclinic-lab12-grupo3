# PetClinic Laboratorio 12 Grupo 3

Proyecto de Construcción y Pruebas de Software para implementar y comprobar los controladores de Vet, Owner, Specialty y Visit. La base común prepara H2, el aislamiento de pruebas y la ejecución automática en GitHub para trabajar entre dos integrantes.

Referencia del docente: [jgomezz/petclinic_integration_test](https://github.com/jgomezz/petclinic_integration_test).

## Requisitos y ejecución

- JDK 21 en la terminal, el SDK del proyecto y el ejecutor de Maven de IntelliJ.
- Maven Wrapper incluido; conexión a Internet para la primera descarga de Maven y dependencias.
- Git.

```bash
git clone https://github.com/luiscucho-oss/petclinic-lab12-grupo3.git
cd petclinic-lab12-grupo3
./mvnw clean test -Dspring.profiles.active=h2
```

En Windows utilizar `mvnw.cmd`. Con Maven instalado también se puede ejecutar `mvn clean test -Dspring.profiles.active=h2`.

H2 funciona en memoria y carga `src/main/resources/schema.sql` y `data.sql`; no hace falta instalar MySQL para probar. Los resultados se guardan en `target/surefire-reports/` y los logs en `target/test-logs/`.

Para iniciar la aplicación:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

El ejemplo disponible es `GET http://localhost:8080/pets/1`. Los controladores propios utilizan rutas como `/owners` y `/visits`; el prefijo `/api` corresponde a Spring Data REST.

## Base común

- Se conserva el ejemplo Pet y sus pruebas.
- Las entidades Vet, Owner, Specialty y Visit ya existen; cada responsable completa los campos y las capas necesarios.
- Las pruebas usan H2 por defecto y una base distinta por contexto de Spring.
- `support/IntegrationTest.java` configura Spring Boot, MockMvc, H2 y rollback después de cada prueba. Los ejemplos reales de Pet ya la utilizan.
- `DatabaseSeedTest` comprueba los datos compartidos y sus registros de referencia.
- Mockito se carga como agente al iniciar la JVM de pruebas, conservando el agente de JaCoCo.
- GitHub Actions ejecuta las pruebas con Java 21 en las ramas de trabajo y los pull requests a `main`.

## Reparto de trabajo

| Responsable | Entidades | Rama | Entregables |
|---|---|---|---|
| Luis, `luiscucho-oss` | Vet y Specialty | `feature/vet-specialty` | Funcionalidad, pruebas reales, pruebas con Mockito y evidencias |
| Segundo integrante, por registrar | Owner y Visit | `feature/owner-visit` | Funcionalidad, pruebas reales, pruebas con Mockito y evidencias |

Los contratos, casos y comandos se detallan en [docs/PLAN_LAB12.md](docs/PLAN_LAB12.md).

La ejecución local de la base y sus limitaciones se registran en [docs/VALIDACION_BASE.md](docs/VALIDACION_BASE.md).

La guía Word solicita los cuatro archivos `XxxControllerTest`. [TASKS_INTEGRATION.md](TASKS_INTEGRATION.md), conservado del docente, contiene indicaciones adicionales sobre Mockito y Git. Este reparto incluye también los archivos `XxxControllerMockitoTest` y mantiene los cuatro ejercicios del Word.

## Colaboración

1. Publicar la base común antes de crear las ramas personales.
2. El compañero acepta la invitación y clona este repositorio.
3. Cada integrante configura su nombre y correo de Git y crea su rama desde `main` actualizado.
4. Implementa y prueba sus entidades, sube commits propios y abre un pull request hacia `main` de este repositorio.
5. El otro integrante revisa el cambio antes de fusionarlo.
6. Al integrar ambos trabajos, ejecutar la suite completa y reunir las capturas reales de pruebas, commits, pull requests y revisiones.

## Aportes y evidencias

| Integrante | Aporte previsto | Estado |
|---|---|---|
| Luis, `luiscucho-oss` | Base común | Revisar el commit de preparación y su ejecución en Actions |
| Luis, `luiscucho-oss` | Vet y Specialty | Pendiente |
| Segundo integrante | Owner y Visit | Pendiente |

Conflictos resueltos: todavía no se ha registrado ninguno. Si ocurre uno, documentar el archivo, la causa, la solución y el commit. Registrar únicamente conflictos y revisiones que hayan ocurrido.

Las tres conclusiones se redactarán a partir de los resultados al finalizar los ejercicios.
