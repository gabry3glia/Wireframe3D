# Wireframe3D
A little wireframe 3D renderer I made with [toolbox](https://github.com/gabry3glia/toolbox) and some math!

## How to run
The entry point is the `public static void main(String[] args)` in `Engine.java`.

## Features
The renderer can draw a 3D wireframe mesh (vertices and edges) at a given position, with a given rotation and scale.\
If a vertex is outside of the view frustum it gets clipped away.\
The program is also capable of performing near/far plane intersection for 3D lines, which allows you to see parts of the mesh even if its halfway outside of the view frustum, instead of making the entire line disappear.\
The engine also performs back/front face culling if requested by calling `renderer.enableFaceCulling(Renderer.BACK_FACE);`.

## How to use
You can set the projection mode (either PERSPETIVE or ORTHOGRAPHIC) via the proper function in Renderer.java
E.g.:
```java
// enables perspecive projection with the given canvas width, height, near and far plane distances and a 70 degrees wide field of view
renderer.perspective(this.screen.getWidth(), this.screen.getHeight(), 0.01f, 1000.0f, 70.f);
// enables orthographic projection with the given canvas width, height, near and far plane distances
renderer.orthographic(getWindowWidth(), getWindowHeight(), 10f, 1000.0f);
```

In order to render a mesh you must first create the mesh!\
I made some simple shapes you can use on the fly like box, cylinder and pyramid.
```java
// creates a prysm with 100 units radius, 100 units height, 10 edges and makes sure every face is made of triangles (this will also automatically calculate the face normals)
mesh = Mesh.cylinder(100, 100, 10, true);
```

Not only you must know what to draw, but also where to draw it!\
This is where the `Transform` object comes in handy.\
```java
// creates a new transform object (basically a matrix)
transform = new Transform();
// moves the transform position to (0, 0, -140)
transform.position.setZ(-140);
```

Finally you'll need the camera to view everything from. You can easily create one and leave it there at the world origin by doing:
```java
Camera camera = new Camera();
```
However, you can write your own camera controller from scratch. I wrote a basic one you can use by calling `Camera.cameraController(input, camera, 2f, 2f);` inside of `Template.update()`.\
With it you can move with WASD, SPACE (to go up), LEFT SHIFT (to go down) and the ARROW KEYS to look around.

Now you can render the mesh with the following code (inside of `Template.render()`):
```java
// renders the given mesh with the specified position, rotation and scale and viewing everything from the given camera
renderer.drawMesh(mesh, transform, camera);
```

## How it works
I took this as a personal challenge, so no AI was used for deriving the math nor writing any of this code.\
I also did't want to use matrices a the goal was to write the full equations and this also avoids possible delays caused by the use of Java classes for handling them (remember this runs all on the CPU).

The main math concepts that make this work are:
- projection equations
- 3D line-plane intersection
- vector operations

### Handling a draw call
For every draw call the engine performs the following operations:
1. moves the mesh vertices from local to world space, relative to the world origin (applies the transform matrix)
2. moves the mesh vertices from world to view space, relative to the camera position (applies the view matrix)
3. converts the mesh vertices from view space to NDC (normalized device coordinates, ranged [-1, 1] for horizontal and vertical components and [0, 1] for the depth component)
4. projects the mesh vertices from NDC to screen space: with this step the 3D vertices become 2D points on the flat canvas and there they get drawn
5. connects the projected screen space vertices with lines if needed (for each line the engine intersects them with the near and far planes to cut the excess parts that are not visible)

The convertion at step 3) happens differently depending on the user choice:
- PERSPECTIVE: using triangle similarities the screen coordinates can be found by checking the individual x and y components of the vertex
- ORTHOGRAPHIC: the x and y components are just copied while the depth component is discarded
This helps identifying vertices that are outside of the frustum as their values will be out of range [-1, 1] (or [0, 1] for the depth component).

The projection at step 4) uses two proportions to go from the [-1, 1] ranges to the [0, width] and [0, height] ranges.

Between step 3) and 4) are also performed the line-plane intersections.

### Handling line-place intersection
Triangle similarities can be used again to intersect a 3D line with a 3D plane. In this case the intersection is given by simple equations, as the near and far planes are orthogonal to the forward camera vector once the whole world is transformed to the camera coordinate system (the view space).\
Once the intersection points are found, they take the place of the original line vertices and the new line is rendered.

In order to appreciate the effect you can run the engine with the following parameters:
```java
renderer.perspective(this.screen.getWidth(), this.screen.getHeight(), 100f, 300.0f, 70.f);
renderer.disableFaceCulling();
```

### Registering a custom mesh
Meshes are registered as arrays of vertices (defined in local space, meaning the origin will be the center of the mesh) and arrays of indices.\
The order in which the lines will connect the vertices is set by using the vertices indices.\
Each array of indices makes a face of the mesh. The lines will connect the vertex corresponding to a vertex index with the following one, wrapping around to the first index of the face in case it gets to the end of the face (indices) array.\

A cube can be defined as follows:
```java
float side = 100.f;

// defines the vertices
Vector3[] vertices = new Vector3[]{
    new Vector3(-side, -side, side),    // 0
    new Vector3(side, -side, side),     // 1
    new Vector3(side, side, side),      // 2
    new Vector3(-side, side, side),     // 3
    new Vector3(-side, -side, -side),   // 4
    new Vector3(side, -side, -side),    // 5
    new Vector3(side, side, -side),     // 6
    new Vector3(-side, side, -side)     // 7
};

// defines the faces
int[][] faces = new int[][] {
	new int[] { 0, 1, 2, 3 }, 			      // front
	new int[] { 1, 5, 6, 2 }, 			      // right
	new int[] { 3, 2, 6, 7 }, 		        // top
	new int[] { 4, 5, 1, 0 }, 			      // bottom
	new int[] { 4, 0, 3, 7 }, 			      // left
	new int[] { 5, 4, 7, 6 }  			      // back
};

// sets the face normals
normals[0] = new Vector3(0, 0, 1));   	// front
normals[1] = new Vector3(1, 0, 0));   	// right
normals[2] = new Vector3(0, 1, 0));   	// top
normals[3] = new Vector3(0, -1, 0));  	// bottom
normals[4] = new Vector3(-1, 0, 0));  	// left
normals[5] = new Vector3(0, 0, -1));  	// back
        
Mesh cube = new Mesh(vertices, faces, normals);
```

### Handling face-culling
In order to avoid drawing non visible faces the engine checks if the face normal is not aligned with the vector differece between the camera position and the face vectors positions, and by using dot similarity (dot product < 0).

When a mesh is to be rendered, the engine loops over its faces and for each face it loops over the all face contiguous vertices pairs to get the edges.

The face culling check is performed every time the renderer is starting to iterate on a new face.

## TO-DO
Here are some things I want to do for this project:
- refine and reorganize the code
- document it properly
- fix an annoying bug in the cone mesh generation function
- rename the "cylinder" function "prism"
- rename the "cone" function "pyramid"
- add spheres as prefab shapes
- add a run script `*.sh` script
