<p align="center">
    <img width="200" src="https://github.com/Intro-CompuMovil/SeekAndSolve/blob/main/Kit%20de%20Marca/SeekAndSolveLogoNoSloganNobg.png">
</p>
<!--horizontal divider(gradiant)-->
<img src="https://user-images.githubusercontent.com/73097560/115834477-dbab4500-a447-11eb-908a-139a6edaec5c.gif">
<p align="center">
 <a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&pause=1000&color=010400&background=FFC83DDD&center=true&vCenter=true&width=435&lines=Explora%2C+Resuelve%2C+Conquista" alt="Typing SVG" /></a>
</p>

## Nosotras somos las 4 divinas 💅:

| [![Alejandro Bermúdez Fajardo](https://avatars.githubusercontent.com/u/133521849?v=4)](https://github.com/alexoberco) | [![Carlos Andrés Carrero Sandoval](https://avatars.githubusercontent.com/u/155045111?v=4)](https://github.com/sharly-dev) | [![Daniel Santiago Silva Gomez](https://avatars.githubusercontent.com/u/178740893?v=4)](https://github.com/silvag-daniels) | [![Édgar Julián González Sierra](https://avatars.githubusercontent.com/u/169292875?v=4)](https://github.com/ejgonzalez16) |
|:--:|:--:|:--:|:--:|
| [**Alejandro Bermúdez Fajardo**](https://github.com/alexoberco) | [**Carlos Andrés Carrero Sandoval**](https://github.com/sharly-dev) | [**Daniel Santiago Silva Gomez**](https://github.com/silvag-daniels) | [**Édgar Julián González Sierra**](https://github.com/ejgonzalez16) |


<h3> Indice: </h3>

- [📖 Descripción](#-descripción)
- [⚙️ Servicios y hardware utilizados](#%EF%B8%8F-servicios-y-hardware-utilizados)
- [💬 ​DCU (Diagrama de Casos de Uso)](#-dcu-diagrama-de-casos-de-uso)
- [📦 DC (Diagrama de Clases)](#dc-diagrama-de-clases)
- [🎨 Pantallas principales de la aplicación (mockups)](#-pantallas-principales-de-la-aplicación-mockups)


<!--horizontal divider(gradiant)-->
<img src="https://user-images.githubusercontent.com/73097560/115834477-dbab4500-a447-11eb-908a-139a6edaec5c.gif">

<h3>📖 Descripción</h3>

planteamos una app móvil en forma de juego, compuesta por carreras de observación creadas por usuarios y creadores, que consisten en retos, desafíos y pistas que deben seguir los usuarios para llegar a un tesoro digital y obtener recompensas dentro de la aplicación (puntos, coleccionables, medallas, logros, etc.).

<h3>⚙️ Servicios y hardware utilizados</h3>

- **📷 Uso de la Cámara**
  - Al llegar a cada una de las estaciones o checkpoints dentro de la carrera de observación, los jugadores deberán tomarse una foto en el lugar.

- **📍 Posicionamiento GPS, mapas y rutas**
  - Cuando se toma la foto se verifica que la ubicación actual de los jugadores coincida con la ubicación del checkpoint.
  - Tendremos mapas interactivos, usando el OSM, para mostrar la ubicación de inicio, fin y checkpoints de las carreras de observación.
  - Además, los jugadores pueden ver la ubicación aproximada y rutas hacia los tesoros recibiendo pistas para encontrarlos.

- **📱 Uso del sensor acelerómetro**
  - Dentro de algunos acertijos y trivias de la carrera de observación se les pedirá a los jugadores que agiten el teléfono para activar la pantalla que muestra la siguiente pregunta, y otras acciones.

- **🌀 Uso del sensor giroscopio**
  - Dentro de algunos acertijos y trivias de la carrera de observación se les pedirá a los jugadores que desenrollen pergaminos, y otras acciones.

- **✋ Uso del sensor de proximidad**
  - Dentro de algunos acertijos y trivias de la carrera de observación se les pedirá a los jugadores que acerquen su mano al sensor de proximidad para "descubrir" preguntas, respuestas, y otras acciones.

- **💡 Uso del sensor de luz**
  - Dentro de algunos acertijos y trivias de la carrera de observación se les pedirá a los jugadores que muevan el teléfono de un lugar oscuro a uno iluminado para "iluminar" algún mapa antiguo y encontrar la respuesta, y otras acciones.

- **⌚ Integración con Wearables (sensor de pulso cardiaco)**
  - Compatibilidad con dispositivos wearables para medir el ritmo cardiaco de los usuarios mientras completan las carreras de observación. Esto para dar un incentivo extra a aquellos usuarios que realicen actividad física en la búsqueda de tesoros.

- **🗂️ Uso del almacenamiento multimedia y texto**
  - Se almacenará en Firebase Realtime Database toda la información de los jugadores, las carreras de observación, las métricas, los tesoros, etc.
  - Las fotografías se almacenarán en Firebase Storage.

- **🌐 Implementación en un framework diferente a Android**
  - La aplicación va a ser desarrollada en su totalidad en el framework Flutter, el cual cuenta con la capacidad de desarrollo híbrido.

- **🔗 Conectarse a Backend de terceros**
  - Desarrollo de una API REST propia que se conectará a la base de datos de Firebase.



<h3>💬 ​DCU (Diagrama de Casos de Uso)</h3>

<p align="center">
    <img width="1000" src="https://github.com/Intro-CompuMovil/SeekAndSolve/blob/main/Diagramas/Diagrama%20de%20Casos%20de%20Uso.jpg">
</p>

<h3>DC (Diagrama de Clases)</h3>

<p align="center">
    <img width="1000" src="https://github.com/Intro-CompuMovil/SeekAndSolve/blob/main/Diagramas/Diagrama%20de%20Clases%20-%20SeekAndSolve.png">
</p>


<h3>🎨 Pantallas principales de la aplicación (mockups)</h3>

planteamos una app móvil en forma de juego, compuesta por carreras de observación creadas por usuarios y creadores, que consisten en retos, desafíos y pistas que deben seguir los usuarios para llegar a un tesoro digital y obtener recompensas dentro de la aplicación (puntos, coleccionables, medallas, logros, etc.).
