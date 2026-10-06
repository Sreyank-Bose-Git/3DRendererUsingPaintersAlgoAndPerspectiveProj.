package renderer;

import datatypes.*;
import main.RendererPanel;
import objects.Line;
import objects.Triangle;
import objects.Vertex;

import java.awt.*;

public class Renderer {

    RendererPanel rendererPanel;

    double focalLength = 1;

    public double nearPlane = 0.01;
    public double farPlane = 1000;

    public Renderer(RendererPanel rendererPanel) {

        this.rendererPanel = rendererPanel;
    }

    public void update() {

    }

    public void draw(Graphics2D g2D) {

        // Clear temporary geometry from previous frame
        rendererPanel.scripts.tempTriangleCount = 0;
        rendererPanel.scripts.tempVertexCount = 0;
        rendererPanel.scripts.tempLineCount = 0;

        // =========================================================
        // GENERATE TEMPORARY TRIANGLES
        // =========================================================

        Integer[] indices =
                rendererPanel.scripts.paintersAlgorithm(
                        rendererPanel.world.triangles,
                        rendererPanel.camera
                );

        for (Integer index : indices) {

            if (rendererPanel.world.triangles[index] == null) {
                continue;
            }

            Triangle triangle =
                    rendererPanel.world.triangles[index];

            if (!triangle.opaque) {
                continue;
            }

            // Clips triangle and stores result in temp arrays
            rendererPanel.scripts.renderTriangle(
                    triangle,
                    rendererPanel.camera,
                    nearPlane, farPlane
            );
        }


        // =========================================================
        // GENERATE TEMPORARY LINES
        // =========================================================

        for (int i = 0; rendererPanel.world.lines[i] != null; i++) {

            Line line = rendererPanel.world.lines[i];

            if (!line.opaque) {
                continue;
            }

            // Clips line and stores result in temp arrays
            rendererPanel.scripts.renderLine(
                    line,
                    rendererPanel.camera,
                    nearPlane, farPlane
            );
        }


        // =========================================================
        // RENDER TEMPORARY TRIANGLES
        // =========================================================

        for (int i = 0;
             i < rendererPanel.scripts.tempTriangleCount;
             i++) {

            Triangle triangle =
                    rendererPanel.scripts.tempTriangles[i];

            Vertex v1 =
                    rendererPanel.scripts.tempVertices[triangle.index1];

            Vertex v2 =
                    rendererPanel.scripts.tempVertices[triangle.index2];

            Vertex v3 =
                    rendererPanel.scripts.tempVertices[triangle.index3];


            // Project
            Vector2 screenCoords1 =
                    rendererPanel.scripts.perspectiveProjection(
                            v1.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );

            Vector2 screenCoords2 =
                    rendererPanel.scripts.perspectiveProjection(
                            v2.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );

            Vector2 screenCoords3 =
                    rendererPanel.scripts.perspectiveProjection(
                            v3.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );


            if (screenCoords1 == null ||
                    screenCoords2 == null ||
                    screenCoords3 == null) {

                continue;
            }


            // Convert to pixel coordinates
            Pixel screenPixel1 =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords1,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );

            Pixel screenPixel2 =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords2,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );

            Pixel screenPixel3 =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords3,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );


            // Draw
            g2D.setColor(triangle.color);

            g2D.fillPolygon(
                    new int[] {
                            screenPixel1.x,
                            screenPixel2.x,
                            screenPixel3.x
                    },
                    new int[] {
                            screenPixel1.y,
                            screenPixel2.y,
                            screenPixel3.y
                    },
                    3
            );
        }


        // =========================================================
        // RENDER TEMPORARY LINES
        // =========================================================

        for (int i = 0;
             i < rendererPanel.scripts.tempLineCount;
             i++) {

            Line line =
                    rendererPanel.scripts.tempLines[i];

            Vertex v1 =
                    rendererPanel.scripts.tempVertices[line.index1];

            Vertex v2 =
                    rendererPanel.scripts.tempVertices[line.index2];


            // Project
            Vector2 screenCoords1 =
                    rendererPanel.scripts.perspectiveProjection(
                            v1.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );

            Vector2 screenCoords2 =
                    rendererPanel.scripts.perspectiveProjection(
                            v2.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );


            if (screenCoords1 == null ||
                    screenCoords2 == null) {

                continue;
            }


            // Convert to pixels
            Pixel screenPixel1 =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords1,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );

            Pixel screenPixel2 =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords2,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );


            // Draw
            g2D.setColor(line.color);

            g2D.setStroke(
                    new BasicStroke(line.thickness)
            );

            g2D.drawLine(
                    screenPixel1.x,
                    screenPixel1.y,
                    screenPixel2.x,
                    screenPixel2.y
            );
        }


        // =========================================================
        // RENDER TEMPORARY VERTICES
        // =========================================================

        for (int i = 0;
             i < rendererPanel.scripts.tempVertexCount;
             i++) {

            Vertex vertex =
                    rendererPanel.scripts.tempVertices[i];

            if (!vertex.opaque) {
                continue;
            }


            Vector2 screenCoords =
                    rendererPanel.scripts.perspectiveProjection(
                            vertex.coordinates,
                            focalLength,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight,
                            rendererPanel.camera,
                            nearPlane, farPlane
                    );


            if (screenCoords == null) {
                continue;
            }


            Pixel screenPixel =
                    rendererPanel.scripts.pixelatedCoordinates(
                            screenCoords,
                            rendererPanel.screenWidth,
                            rendererPanel.screenHeight
                    );


            g2D.setColor(vertex.color);

            g2D.fillOval(
                    screenPixel.x - (vertex.radius / 2),
                    screenPixel.y - (vertex.radius / 2),
                    vertex.radius,
                    vertex.radius
            );
        }
    }
}
