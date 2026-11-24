# Registro de prompts de Gemini

> Nota: el historial disponible conserva las mejoras realizadas y parte de las respuestas, pero no la transcripción literal completa de cada prompt. Por eso, la primera columna contiene resúmenes fieles del pedido y no citas textuales.

| Prompt que escribí (resumen) | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| Implementar búsqueda local por título en las publicaciones ya descargadas, respetando MVVM, los filtros por usuario y los estados existentes; no agregar llamadas a la API. | Propuso guardar el texto de búsqueda y filtrar localmente las publicaciones del ViewModel, además de agregar un campo de búsqueda en la pantalla de lista. | Se mantuvo el filtrado local con `contains` sin distinguir mayúsculas y minúsculas. Se revisó que la búsqueda opere sobre los resultados del usuario seleccionado y no ejecute llamadas de red adicionales. | La búsqueda solicitada es sobre los datos ya descargados. Mantenerla en ViewModel + UI respeta las capas indicadas para esta mejora y evita tráfico innecesario.
| Agregar eliminación de publicaciones desde el detalle con `DELETE posts/{id}`, mostrar la respuesta y conservar MVVM + Repository. | Gemini propuso cambios en la API, el repositorio, el ViewModel y la pantalla de detalle para iniciar la eliminación y mostrar sus estados. | Se revisó la respuesta HTTP con `Response<Unit>` en la API y `isSuccessful` en el Repository. Se conserva y relanza `CancellationException`. Como JSONPlaceholder simula DELETE y no persiste cambios, el ViewModel oculta localmente el id eliminado al volver a cargar la lista. | El laboratorio pide revisar el código de respuesta y no dejar que Retrofit ni `HttpException` lleguen al ViewModel. El filtrado local evita que el elemento reaparezca inmediatamente por la naturaleza simulada del servicio.
| Agregar un interceptor de OkHttp que incluya `Accept: application/json` en todas las solicitudes, sin cambiar la URL base ni el registro HTTP. | Gemini propuso un interceptor para construir cada solicitud con el encabezado `Accept` y conservar el `HttpLoggingInterceptor`. | Se colocó el interceptor de encabezado antes del interceptor de logging y se usó `.header(...)`. No se modificaron `PostApi`, Repository, ViewModel ni pantallas. | Al ponerlo antes del logging, Logcat también muestra el encabezado enviado. `.header(...)` establece un único valor para evitar duplicados. La solicitud se compiló y se verificó en Logcat.

## Revisión técnica

- Las mejoras se revisaron dentro de la arquitectura MVVM + Repository existente.
- El ViewModel no depende de Retrofit, `PostApi` ni `HttpException`.
- La búsqueda filtra los datos descargados; el interceptor solo configura solicitudes HTTP; la eliminación comprueba el estado HTTP en el Repository.
- Se compiló el proyecto después de los cambios y se comprobó el encabezado `Accept: application/json` en Logcat.
