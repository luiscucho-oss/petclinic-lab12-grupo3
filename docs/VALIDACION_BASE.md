# Validación de la base común

Fecha: 3 de noviembre de 2026. Entorno: JDK 21 y Maven 3.9.11. Esta verificación corresponde a la preparación inicial, antes de implementar los ejercicios de Vet, Owner, Specialty y Visit.

## Resultado de las pruebas

Se compilaron las fuentes de producción y las de pruebas, se cargaron los recursos y se ejecutaron las 29 pruebas disponibles con Surefire: 0 fallos, 0 errores y 0 omitidas. Se utilizaron las dependencias ya descargadas y un repositorio Maven temporal.

Comando de las fases ejecutadas, con el JDK 21 seleccionado y el repositorio local disponible:

```bash
mvn -o -Dspring.profiles.active=h2 resources:resources compiler:compile resources:testResources compiler:testCompile surefire:test
```

| Clase | Pruebas |
|---|---|
| PetClinicApplicationTests | 1 |
| PetServiceTest | 7 |
| PetServiceMockitoTest | 7 |
| PetControllerTest | 7 |
| PetControllerMockitoTest | 5 |
| DatabaseSeedTest | 2 |
| Total | 29 |

Las clases vacías del docente no aportan pruebas al total. `DatabaseSeedTest` verifica los conteos y las referencias que utilizarán los dos integrantes. Las pruebas reales de Pet se ejecutan con rollback y las bases H2 se separan por contexto.

## Verificación completa pendiente

El comando habitual `mvn clean test -Dspring.profiles.active=h2` se detuvo antes de ejecutar pruebas porque faltan dependencias de JaCoCo 0.8.13 y el entorno de preparación no puede conectarse a Maven Central. La ejecución de 29 pruebas descrita arriba no incluye instrumentación ni informe de cobertura.

Con acceso a Maven Central, ejecutar el comando completo:

```bash
./mvnw clean test -Dspring.profiles.active=h2
```

El workflow de GitHub Actions está preparado para ejecutarlo cuando se publique la base. Su resultado y el de la ejecución completa deben comprobarse antes de dar la base por validada en GitHub.
