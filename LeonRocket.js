import * as THREE from 'three';
import { PhysicEntity } from './PhysicEntity.js';

export class LeonRocket extends PhysicEntity {
    constructor(labyrinth, initialDirection, initialPosition) {
        super(labyrinth);

        this.direction.copy(initialDirection);
        this.directionVel.copy(initialDirection); // Rockets move along their direction vector
        this.position.copy(initialPosition);

        // Rocket-specific physics properties
        this.radius = 0.125;
        this.impulse = 0.2; // Initial speed, as in Java. Note: This might be very slow depending on world scale and deltaTime.
        this.friction = 0.0;
        this.applyfriction = false;
        this.angle = 0.0; // No angular change for a simple rocket
        this.applyanglefriction = false;
        this.rotationAngle = Math.atan2(this.direction.x, this.direction.z); // Align PhysicEntity's rotationAngle with initialDirection

        // 3D Mesh for the rocket
        const geometry = new THREE.SphereGeometry(this.radius, 8, 8);
        const material = new THREE.MeshStandardMaterial({
            emissive: 0xffff00, // Yellow emissive color
            emissiveIntensity: 2.0,  // Strong emissive intensity
            color: 0xffff00,       // Base color yellow
            //metalness: 0.2,        // Optional: slight metallic look
            //roughness: 0.5         // Optional: control shininess
        });
        this.mesh = new THREE.Mesh(geometry, material);
        this.mesh.position.copy(this.position);
        
        if (this.labyrinth && this.labyrinth.scene) {
            this.labyrinth.scene.add(this.mesh);
        } else {
            console.error("Labyrinth scene reference not available for LeonRocket mesh.");
        }

        // Light for the rocket
        this.light = new THREE.PointLight(0xffaa00, 1, 50); // Orange-yellow light, intensity 1, affects up to 50 units
        this.light.position.copy(this.position);
        if (this.labyrinth && this.labyrinth.scene) {
            this.labyrinth.scene.add(this.light);
        } else {
            console.error("Labyrinth scene reference not available for LeonRocket light.");
        }
        
        this.active = true; // Flag to indicate if the rocket is in flight
        this.updateCurrentCell(); // Initialize m_cell based on starting position
    }

    update(deltaTime) {
        if (!this.active) {
            return;
        }

        // Store previous position for collision checking or other logic if needed.
        // const prevPosition = this.position.clone();

        // PhysicEntity's updatePosition handles movement and collision detection
        super.updatePosition(deltaTime); // This updates this.position and this.applynormal

        if (this.applynormal) { // Collision occurred
            this.active = false;
            
            if (this.labyrinth && this.labyrinth.scene) {
                this.labyrinth.scene.remove(this.mesh);
                this.labyrinth.scene.remove(this.light);
            }
            
            // Dispose of Three.js resources
            if (this.mesh) {
                if (this.mesh.geometry) this.mesh.geometry.dispose();
                if (this.mesh.material) this.mesh.material.dispose();
            }
            if (this.light) {
                this.light.dispose(); // PointLight doesn't have a complex geometry/material like mesh, but good practice.
            }
            
            // console.log("Rocket collided and deactivated.");

        } else {
            // No collision, update mesh and light positions
            if (this.mesh) {
                this.mesh.position.copy(this.position);
            }
            if (this.light) {
                this.light.position.copy(this.position);
            }
        }
    }
}
