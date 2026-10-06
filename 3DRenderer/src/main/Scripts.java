package main;

import datatypes.*;
import objects.Line;
import objects.Triangle;
import objects.Vertex;
import renderer.Camera;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Scripts {

    public Triangle[] tempTriangles;
    public Vertex[] tempVertices;
    public Line[] tempLines;

    public int tempLineCount = 0;
    public int tempVertexCount = 0;
    public int tempTriangleCount = 0;

    RendererPanel rendererPanel;

    public Scripts(RendererPanel rendererPanel, int tTri, int tVert, int tLine) {
        this.rendererPanel = rendererPanel;

        tempTriangles = new Triangle[tTri];
        tempLines = new Line[tLine];
        tempVertices = new Vertex[tVert];
    }

    public static Vector3 cameraToWorld(Vector3 cameraPoint, Camera camera) {

        double radYaw = Math.toRadians(camera.rotation.y);
        double radPitch = Math.toRadians(camera.rotation.x);

        double cosYaw = Math.cos(radYaw);
        double sinYaw = Math.sin(radYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);

        // Undo pitch rotation
        double dy = cameraPoint.y * cosPitch + cameraPoint.z * sinPitch;
        double z1 = -cameraPoint.y * sinPitch + cameraPoint.z * cosPitch;

        // Undo yaw rotation
        double dx = cameraPoint.x * cosYaw + z1 * sinYaw;
        double dz = -cameraPoint.x * sinYaw + z1 * cosYaw;

        // Move back to world position
        return new Vector3(
                dx + camera.coordinates.x,
                dy + camera.coordinates.y,
                dz + camera.coordinates.z
        );
    }

    public Vertex getClosestVertex(Vector3 worldPoint) {

        Vertex closestVertex = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (int i = 0; rendererPanel.world.vertices[i] != null; i++) {

            Vertex vertex = rendererPanel.world.vertices[i];

            double dx = vertex.coordinates.x - worldPoint.x;
            double dy = vertex.coordinates.y - worldPoint.y;
            double dz = vertex.coordinates.z - worldPoint.z;

            double distanceSquared =
                    dx * dx +
                            dy * dy +
                            dz * dz;

            if (distanceSquared < closestDistanceSquared) {

                closestDistanceSquared = distanceSquared;
                closestVertex = vertex;
            }
        }

        return closestVertex;
    }

    public Vector3 worldToCamera(Vector3 worldPoint, Camera camera) {

        double radYaw   = Math.toRadians(camera.rotation.y);
        double radPitch = Math.toRadians(camera.rotation.x);

        double cosYaw   = Math.cos(radYaw);
        double sinYaw   = Math.sin(radYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);

        // Translate relative to camera
        double dx = worldPoint.x - camera.coordinates.x;
        double dy = worldPoint.y - camera.coordinates.y;
        double dz = worldPoint.z - camera.coordinates.z;

        // Yaw
        double x1 = dx * cosYaw - dz * sinYaw;
        double z1 = dx * sinYaw + dz * cosYaw;

        // Pitch
        double y2 = dy * cosPitch - z1 * sinPitch;
        double z2 = dy * sinPitch + z1 * cosPitch;

        return new Vector3(x1, y2, z2);
    }

    public Vector3[] clipLine(Vector3 a, Vector3 b, double plane, boolean keepGreater) {

        boolean aInside = keepGreater ? (a.z >= plane) : (a.z <= plane);
        boolean bInside = keepGreater ? (b.z >= plane) : (b.z <= plane);

        // Both outside
        if (!aInside && !bInside) {
            return null;
        }

        // Both inside
        if (aInside && bInside) {
            return new Vector3[] { a, b };
        }

        // One point is outside -> find intersection
        double denom = (b.z - a.z);
        double t = (Math.abs(denom) < 1e-9) ? 0.0 : (plane - a.z) / denom;

        Vector3 intersection = new Vector3(
                a.x + t * (b.x - a.x),
                a.y + t * (b.y - a.y),
                plane
        );

        if (!aInside) {
            return new Vector3[] { intersection, b };
        } else {
            return new Vector3[] { a, intersection };
        }
    }

    public void renderLine(
            Line line,
            Camera camera,
            double nearPlane,
            double farPlane) {

        Vertex v1 = rendererPanel.world.vertices[line.index1];
        Vertex v2 = rendererPanel.world.vertices[line.index2];

        Vector3 ca = worldToCamera(v1.coordinates, camera);
        Vector3 cb = worldToCamera(v2.coordinates, camera);

        // 1. Clip against near plane
        Vector3[] nearClipped = clipLine(ca, cb, nearPlane, true);
        if (nearClipped == null) {
            return;
        }

        // 2. Sequentially clip result against far plane
        Vector3[] finalClipped = clipLine(nearClipped[0], nearClipped[1], farPlane, false);
        if (finalClipped == null) {
            return;
        }

        // First temporary vertex
        Vector3 worldPoint1 = cameraToWorld(finalClipped[0], camera);
        int index1 = tempVertexCount;

        Vertex v1_c = getClosestVertex(worldPoint1);

        tempVertices[tempVertexCount++] = new Vertex(
                worldPoint1,
                v1_c.color,
                v1_c.radius,
                v1_c.opaque
        );

        // Second temporary vertex
        Vector3 worldPoint2 = cameraToWorld(finalClipped[1], camera);
        int index2 = tempVertexCount;

        Vertex v2_c = getClosestVertex(worldPoint2);

        tempVertices[tempVertexCount++] = new Vertex(
                worldPoint2,
                v2_c.color,
                v2_c.radius,
                v2_c.opaque
        );

        // Temporary line
        tempLines[tempLineCount++] = new Line(
                index1,
                index2,
                line.color,
                line.thickness,
                line.opaque
        );
    }

    public Vector3[] clipPolygon(
            Vector3[] input,
            double plane,
            boolean keepGreater) {

        if (input == null || input.length == 0) {
            return null;
        }

        ArrayList<Vector3> output = new ArrayList<>();

        for (int i = 0; i < input.length; i++) {

            Vector3 current = input[i];
            Vector3 previous = input[(i + input.length - 1) % input.length];

            boolean currentInside = keepGreater ? (current.z >= plane) : (current.z <= plane);
            boolean previousInside = keepGreater ? (previous.z >= plane) : (previous.z <= plane);

            // Outside -> Inside
            if (!previousInside && currentInside) {

                double denom = (current.z - previous.z);
                double t = (Math.abs(denom) < 1e-9) ? 0.0 : (plane - previous.z) / denom;

                output.add(new Vector3(
                        previous.x + t * (current.x - previous.x),
                        previous.y + t * (current.y - previous.y),
                        plane
                ));

                output.add(current);
            }

            // Inside -> Inside
            else if (previousInside && currentInside) {
                output.add(current);
            }

            // Inside -> Outside
            else if (previousInside) {

                double denom = (current.z - previous.z);
                double t = (Math.abs(denom) < 1e-9) ? 0.0 : (plane - previous.z) / denom;

                output.add(new Vector3(
                        previous.x + t * (current.x - previous.x),
                        previous.y + t * (current.y - previous.y),
                        plane
                ));
            }
        }

        if (output.isEmpty()) {
            return null;
        }

        return output.toArray(new Vector3[0]);
    }

    public void renderTriangle(
            Triangle triangle,
            Camera camera,
            double nearPlane,
            double farPlane) {

        Vertex v1 = rendererPanel.world.vertices[triangle.index1];
        Vertex v2 = rendererPanel.world.vertices[triangle.index2];
        Vertex v3 = rendererPanel.world.vertices[triangle.index3];

        Vector3 ca = worldToCamera(v1.coordinates, camera);
        Vector3 cb = worldToCamera(v2.coordinates, camera);
        Vector3 cc = worldToCamera(v3.coordinates, camera);

        // 1. Clip against near plane (keep greater than or equal to nearPlane)
        Vector3[] nearClipped = clipPolygon(
                new Vector3[] { ca, cb, cc },
                nearPlane,
                true
        );

        if (nearClipped == null) {
            return;
        }

        // 2. Sequentially clip near-clipped output against far plane (keep less than or equal to farPlane)
        Vector3[] finalClipped = clipPolygon(
                nearClipped,
                farPlane,
                false
        );

        if (finalClipped == null || finalClipped.length < 3) {
            return;
        }

        int[] indices = new int[finalClipped.length];

        // Store temporary vertices
        for (int i = 0; i < finalClipped.length; i++) {

            Vector3 worldPoint =
                    cameraToWorld(finalClipped[i], camera);

            indices[i] = tempVertexCount;

            Vertex vertex = getClosestVertex(worldPoint);

            tempVertices[tempVertexCount++] = new Vertex(
                    worldPoint,
                    vertex.color,
                    vertex.radius,
                    vertex.opaque
            );
        }

        // Fan triangulation for N vertices -> N - 2 triangles
        for (int i = 1; i < finalClipped.length - 1; i++) {

            tempTriangles[tempTriangleCount++] = new Triangle(
                    indices[0],
                    indices[i],
                    indices[i + 1],
                    triangle.color,
                    triangle.opaque
            );
        }
    }

    public Vector2 perspectiveProjection(
            Vector3 worldPoint,
            double focalLength,
            int width,
            int height,
            Camera camera,
            double nearPlane, double farPlane) {

        double aspectRatio = (double) width / height;

        Vector3 cameraPoint = worldToCamera(worldPoint, camera);

        // Move point forward to the near plane if slightly behind
        if (cameraPoint.z < nearPlane) {
            cameraPoint.z = nearPlane;
        }

        // Prevent floating-point precision dropouts at the far plane boundary
        if (cameraPoint.z > farPlane) {
            if (cameraPoint.z <= farPlane + 0.01) {
                cameraPoint.z = farPlane; // Clamp to avoid null projection dropouts
            } else {
                return null;
            }
        }

        double ndcX =
                (focalLength * cameraPoint.x)
                        / (cameraPoint.z * aspectRatio);

        double ndcY =
                (focalLength * cameraPoint.y)
                        / cameraPoint.z;

        return new Vector2(ndcX, ndcY);
    }

    public Pixel pixelatedCoordinates(Vector2 ndc, int screenWidth, int screenHeight) {
        // Map NDC X [-1, 1] to Screen Pixels [0, screenWidth]
        int x = (int) Math.round((ndc.x + 1.0) * 0.5 * screenWidth);

        // Map NDC Y [1, -1] to Screen Pixels [0, screenHeight]
        int y = (int) Math.round((1.0 - ndc.y) * 0.5 * screenHeight);

        return new Pixel(x, y);
    }

    public Integer[] paintersAlgorithm(Triangle[] triangles, Camera camera) {
        if (triangles == null || triangles.length == 0) {
            return new Integer[0];
        }

        double[] avgData = new double[triangles.length];
        Integer[] indices = new Integer[triangles.length];

        for (int i = 0; i < triangles.length; i++) {
            indices[i] = i;

            if (triangles[i] == null) {
                avgData[i] = -1.0;
                continue;
            }

            double avgX = (rendererPanel.world.vertices[triangles[i].index1].coordinates.x + rendererPanel.world.vertices[triangles[i].index2].coordinates.x + rendererPanel.world.vertices[triangles[i].index3].coordinates.x) / 3.0;
            double avgY = (rendererPanel.world.vertices[triangles[i].index1].coordinates.y + rendererPanel.world.vertices[triangles[i].index2].coordinates.y + rendererPanel.world.vertices[triangles[i].index3].coordinates.y) / 3.0;
            double avgZ = (rendererPanel.world.vertices[triangles[i].index1].coordinates.z + rendererPanel.world.vertices[triangles[i].index2].coordinates.z + rendererPanel.world.vertices[triangles[i].index3].coordinates.z) / 3.0;

            double dx = avgX - camera.coordinates.x;
            double dy = avgY - camera.coordinates.y;
            double dz = avgZ - camera.coordinates.z;

            avgData[i] = (dx * dx) + (dy * dy) + (dz * dz);
        }

        Arrays.sort(indices, Comparator.<Integer>comparingDouble(i -> avgData[i]).reversed());

        return indices;
    }
}