import * as THREE from 'three';

export class Player {
    constructor(camera) {
        this.camera = camera;
        this.position = camera.position;
        this.velocity = new THREE.Vector3();
        this.speed = 0.15;
        this.health = 100;
        this.moveForward = false;
        this.moveBackward = false;
        this.moveLeft = false;
        this.moveRight = false;
    }

    handleKeyDown(event) {
        switch (event.code) {
            case 'KeyW': this.moveForward = true; break;
            case 'KeyS': this.moveBackward = true; break;
            case 'KeyA': this.moveLeft = true; break;
            case 'KeyD': this.moveRight = true; break;
        }
    }

    handleKeyUp(event) {
        switch (event.code) {
            case 'KeyW': this.moveForward = false; break;
            case 'KeyS': this.moveBackward = false; break;
            case 'KeyA': this.moveLeft = false; break;
            case 'KeyD': this.moveRight = false; break;
        }
    }

    update(labyrinth) {
        // Calculate movement direction
        const direction = new THREE.Vector3();
        const rotation = this.camera.rotation.y;

        if (this.moveForward) direction.z -= Math.cos(rotation);
        if (this.moveBackward) direction.z += Math.cos(rotation);
        if (this.moveLeft) direction.x -= Math.cos(rotation + Math.PI/2);
        if (this.moveRight) direction.x += Math.cos(rotation + Math.PI/2);

        if (direction.length() > 0) {
            direction.normalize();
            const nextPosition = this.position.clone().add(
                direction.multiplyScalar(this.speed)
            );

            // Check collision with walls
            if (!this.checkCollision(nextPosition, labyrinth)) {
                this.position.copy(nextPosition);
            }
        }
    }

    checkCollision(position, labyrinth) {
        const gridX = Math.floor(position.x);
        const gridZ = Math.floor(position.z);

        // Check surrounding cells
        for (let x = gridX - 1; x <= gridX + 1; x++) {
            for (let z = gridZ - 1; z <= gridZ + 1; z++) {
                if (labyrinth.cells[x]?.[z]?.type === Cell.WALL) {
                    const distance = new THREE.Vector2(
                        position.x - x,
                        position.z - z
                    ).length();
                    if (distance < 0.5) return true;
                }
            }
        }
        return false;
    }

    damage(amount) {
        this.health = Math.max(0, this.health - amount);
        document.getElementById('health').textContent = this.health;
        
        if (this.health <= 0) {
            alert('Game Over!');
            location.reload();
        }
    }
}