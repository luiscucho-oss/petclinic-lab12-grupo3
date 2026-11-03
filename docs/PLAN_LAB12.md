# Plan de trabajo del laboratorio 12

La base permite desarrollar Vet, Owner, Specialty y Visit en dos ramas separadas. Esta etapa prepara la infraestructura y conserva Pet como ejemplo; los cuatro ejercicios quedan a cargo de sus responsables.

## Orden de trabajo

1. Preparar, verificar y publicar la base común.
2. El compañero acepta la invitación y desarrolla Owner y Visit.
3. Luis desarrolla Vet y Specialty en su propia rama.
4. Cada integrante abre un pull request y recibe revisión del otro.
5. Integrar ambos trabajos, ejecutar toda la suite y completar el informe.

## Archivos compartidos y pruebas

Mantener `pom.xml`, los perfiles de aplicación, los scripts SQL, `support/IntegrationTest.java` y el workflow como base común. Coordinar cualquier cambio necesario a esos archivos. No crear implementaciones duplicadas para las entidades existentes.

Los datos iniciales incluyen 6 veterinarios, 10 dueños, 3 especialidades, 13 mascotas, 6 visitas, 8 tipos y 5 asignaciones de especialidades. Utilizarlos para las consultas de referencia. Las pruebas que modifican datos deben crear sus propios registros.

Las pruebas reales utilizan `@IntegrationTest`: no sustituir el servicio ni el repositorio por mocks. El rollback restaura las filas al terminar cada prueba, pero no necesariamente el contador de IDs. Leer el ID de la respuesta de creación y no depender del orden de las pruebas.

Las pruebas con Mockito utilizan `@SpringBootTest`, `@AutoConfigureMockMvc` y `@MockitoBean` del servicio, siguiendo `PetControllerMockitoTest`. Preparar objetos mediante helpers por entidad. Probar los mismos endpoints en ambas modalidades; los mocks no demuestran persistencia.

## Contrato común

| Operación | Ruta | Respuesta |
|---|---|---|
| Listar o filtrar | `GET /{entidad}` | `200`, arreglo JSON |
| Consultar existente | `GET /{entidad}/{id}` | `200`, objeto JSON |
| Consultar inexistente | `GET /{entidad}/{id}` | `404` |
| Crear | `POST /{entidad}` | `201`, objeto con ID asignado |
| Actualizar existente | `PUT /{entidad}/{id}` | `200`, objeto actualizado |
| Actualizar inexistente | `PUT /{entidad}/{id}` | `404` |
| Eliminar existente | `DELETE /{entidad}/{id}` | `200`, siguiendo Pet |
| Eliminar inexistente | `DELETE /{entidad}/{id}` | `404` |
| Datos inválidos | `POST` o `PUT` | `400` |

Utilizar DTO para las respuestas y evitar serializar relaciones JPA bidireccionales. El controlador utiliza un servicio, que accede a su repositorio. El ID de una actualización lo determina la ruta.

Las fechas se expresan como `yyyy-MM-dd`; los costos, como `BigDecimal`. Los campos JSON siguen los DTO: `firstName`, `lastName`, `openHour`, `closeHour`, `petId`, `vetId`, `visitDate`. Nombres SQL como `h_open` y `visit_date` se resuelven en el mapeo de la entidad.

## Luis Vet y Specialty

Rama: `feature/vet-specialty`. Completar DTO, repositorios, servicios, mappers, excepciones y controladores según sea necesario. Vet debe incorporar los campos del esquema que utilice, como `active`; Specialty debe mapear `office`, `h_open` y `h_close`.

| Entidad | Casos |
|---|---|
| Vet | Listado; ID 1 con apellido Carter; inexistente; creación; actualización; filtros por activo; desactivar y reactivar; eliminación existente e inexistente |
| Specialty | Listado; ID 1 con nombre radiology; inexistente; creación; actualización; filtros por nombre y oficina; apertura anterior al cierre; horas de 0 a 23; eliminación existente e inexistente |

Rutas adicionales acordadas:

- `PUT /vets/{id}/deactivate` y `PUT /vets/{id}/reactivate`.
- `GET /vets?active=true`.
- `GET /specialties?name=surgery`.
- `GET /specialties?office=Farewell`.

Entregar `VetControllerTest`, `VetControllerMockitoTest`, `SpecialtyControllerTest` y `SpecialtyControllerMockitoTest`, además de sus evidencias.

## Compañero Owner y Visit

Rama: `feature/owner-visit`. Completar las capas de Owner y Visit. Mantener los campos y relaciones existentes; Visit debe representar `vet_id` y `cost` cuando se implementen las consultas adicionales. Los IDs de dueños, mascotas y veterinarios usados en los registros deben existir.

| Entidad | Casos |
|---|---|
| Owner | Listado; ID 1 con apellido Franklin; inexistente; creación válida; rechazo sin apellido; filtro Davis con 2 resultados; actualización confirmada por GET; eliminación existente e inexistente |
| Visit | Listado; ID 1 con descripción rabies shot; inexistente; creación; actualización confirmada por GET; eliminación; filtros por mascota, veterinario y fechas; conteo y suma de costos por mascota |

Rutas adicionales acordadas y resultados sobre los datos iniciales:

- `GET /owners?lastName=Davis`: 2 dueños.
- `GET /visits?petId=7`: 2 visitas.
- `GET /visits?vetId=2`: 2 visitas.
- `GET /visits?from=2024-01-01&to=2024-12-31`: 2 visitas.
- `GET /visits?vetId=1&from=2024-01-01&to=2024-12-31`: 1 visita.
- `GET /visits/pet/7/count`: número JSON `2`.
- `GET /visits/pet/7/total-cost`: número JSON equivalente a `320.00`.

Entregar `OwnerControllerTest`, `OwnerControllerMockitoTest`, `VisitControllerTest` y `VisitControllerMockitoTest`, además de sus evidencias.

## Comenzar la parte del compañero

Después de aceptar la invitación y una vez publicada la base:

```bash
git clone https://github.com/luiscucho-oss/petclinic-lab12-grupo3.git
cd petclinic-lab12-grupo3
git config user.name "TU_NOMBRE_O_USUARIO"
git config user.email "TU_CORREO_ASOCIADO_A_GITHUB"
git switch main
git pull --ff-only origin main
git switch -c feature/owner-visit
./mvnw clean test -Dspring.profiles.active=h2
```

Luis utiliza `feature/vet-specialty` cuando comience su parte. Cada integrante configura su propia identidad y hace sus propios commits.

Para ejecutar el ejemplo antes de desarrollar:

```bash
./mvnw test -Dspring.profiles.active=h2 -Dtest=PetControllerTest,DatabaseSeedTest
```

Una vez creadas las pruebas de Owner y Visit:

```bash
./mvnw test -Dspring.profiles.active=h2 -Dtest=OwnerControllerTest,OwnerControllerMockitoTest,VisitControllerTest,VisitControllerMockitoTest
```

Al terminar, revisar los cambios, hacer commits y subir la rama:

```bash
git status
git add src/main/java/com/tecsup/petclinic src/test/java/com/tecsup/petclinic
git commit -m "Implementa Owner y Visit con pruebas web"
git push -u origin feature/owner-visit
```

Abrir el pull request contra `luiscucho-oss/petclinic-lab12-grupo3`, con base `main`, y solicitar revisión al otro integrante.

## Verificación y entrega

- Ejecutar `./mvnw clean test -Dspring.profiles.active=h2` en la versión integrada.
- Comprobar HTTP, JSON y persistencia después de crear o actualizar.
- Conservar capturas de las pruebas y las interacciones reales en GitHub.
- Completar el README con nombres, aportes y conflictos que realmente ocurrieron.
- Redactar las tres conclusiones a partir de la ejecución final.

`TASKS_INTEGRATION.md`, conservado del docente, pide además revisiones, protección de main y un conflicto resuelto. La protección se configura en GitHub y no queda activada por este documento. Comprobar las reglas aplicables con el docente y registrar únicamente evidencias reales.
