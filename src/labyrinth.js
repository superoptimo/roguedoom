import * as THREE from 'three';
import { Cell } from './cell.js';

export class Labyrinth {
    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.cells = [];
        this.monsters = [];
        this.keys = [];
        this.gates = [];
    }

    generate() {
        // Initialize cells
        for (let x = 0; x < this.width; x++) {
            this.cells[x] = [];
            for (let z = 0; z < this.height; z++) {
                this.cells[x][z] = new Cell(x, z);
            }
        }

        // Generate maze using recursive backtracking
        this.generateMaze(1, 1);
        
        // Add keys and gates
        this.addKeysAndGates();
        
        // Add monsters
        this.addMonsters();
    }

    generateMaze(x, z) {
        const directions = [
            [0, 2],  // North
            [2, 0],  // East
            [0, -2], // South
            [-2, 0]  // West
        ];

        // Shuffle directions
        directions.sort(() => Math.random() - 0.5);

        this.cells[x][z].visited = true;

        for (const [dx, dz] of directions) {
            const newX = x + dx;
            const newZ = z + dz;
            
            if (this.isValidCell(newX, newZ) && !this.cells[newX][newZ].visited) {
                // Remove walls between cells
                this.cells[x + dx/2][z + dz/2].type = Cell.FLOOR;
                this.cells[newX][newZ].type = Cell.FLOOR;
                this.generateMaze(newX, newZ);
            }
        }
    }

    isValidCell(x, z) {
        return x > 0 && x < this.width - 1 && z > 0 && z < this.height - 1;
    }

    addKeysAndGates() {
        const colors = ['red', 'yellow', 'blue'];
        colors.forEach(color => {
            // Add key
            const keyPos = this.getRandomEmptyCell();
            this.keys.push({
                position: keyPos,
                color: color,
                collected: false
            });

            // Add gate
            const gatePos = this.getRandomWallCell();
            this.gates.push({
                position: gatePos,
                color: color,
                opened: false
            });
        });
    }

    addMonsters() {
        for (let i = 0; i < 5; i++) {
            const pos = this.getRandomEmptyCell();
            this.monsters.push({
                position: new THREE.Vector3(pos.x, 0, pos.z),
                health: 100
            });
        }
    }

    getRandomEmptyCell() {
        let cell;
        do {
            const x = Math.floor(Math.random() * this.width);
            const z = Math.floor(Math.random() * this.height);
            cell = this.cells[x][z];
        } while (cell.type !== Cell.FLOOR);
        return cell;
    }

    getRandomWallCell() {
        let cell;
        do {
            const x = Math.floor(Math.random() * this.width);
            const z = Math.floor(Math.random() * this.height);
            cell = this.cells[x][z];
        } while (cell.type !== Cell.WALL);
        return cell;
    }

    build(scene) {
        // Create geometries
        const wallGeometry = new THREE.BoxGeometry(1, 2, 1);
        const wallMaterial = new THREE.MeshPhongMaterial({ color: 0x808080 });
        const floorMaterial = new THREE.MeshPhongMaterial({ color: 0x404040 });

        // Build maze
        for (let x = 0; x < this.width; x++) {
            for (let z = 0; z < this.height; z++) {
                const cell = this.cells[x][z];
                if (cell.type === Cell.WALL) {
                    const wall = new THREE.Mesh(wallGeometry, wallMaterial);
                    wall.position.set(x, 1, z);
                    wall.castShadow = true;
                    wall.receiveShadow = true;
                    scene.add(wall);
                }
            }
        }

        // Add floor
        const floorGeometry = new THREE.PlaneGeometry(this.width, this.height);
        const floor = new THREE.Mesh(floorGeometry, floorMaterial);
        floor.rotation.x = -Math.PI / 2;
        floor.position.set(this.width/2 - 0.5, 0, this.height/2 - 0.5);
        floor.receiveShadow = true;
        scene.add(floor);

        // Add keys and gates
        this.buildKeysAndGates(scene);
    }

    buildKeysAndGates(scene) {
        const keyGeometry = new THREE.BoxGeometry(0.3, 0.3, 0.3);
        const gateGeometry = new THREE.BoxGeometry(1, 2, 0.1);

        this.keys.forEach(key => {
            const material = new THREE.MeshPhongMaterial({ color: this.getColorValue(key.color) });
            const mesh = new THREE.Mesh(keyGeometry, material);
            mesh.position.set(key.position.x, 0.5, key.position.z);
            scene.add(mesh);
            key.mesh = mesh;
        });

        this.gates.forEach(gate => {
            const material = new THREE.MeshPhongMaterial({
                color: this.getColorValue(gate.color),
                transparent: true,
                opacity: 0.7
            });
            const mesh = new THREE.Mesh(gateGeometry, material);
            mesh.position.set(gate.position.x, 1, gate.position.z);
            scene.add(mesh);
            gate.mesh = mesh;
        });
    }

    getColorValue(color) {
        switch(color) {
            case 'red': return 0xff0000;
            case 'yellow': return 0xffff00;
            case 'blue': return 0x0000ff;
            default: return 0xffffff;
        }
    }

    update(player) {
        // Update monsters
        this.updateMonsters(player);

        // Check key collection
        this.checkKeyCollection(player);

        // Check gate interaction
        this.checkGateInteraction(player);
    }

    updateMonsters(player) {
        this.monsters.forEach(monster => {
            if (monster.health <= 0) return;

            // Simple AI: Move towards player
            const direction = new THREE.Vector3()
                .subVectors(player.position, monster.position)
                .normalize();
            monster.position.add(direction.multiplyScalar(0.05));

            // Attack player if close
            if (monster.position.distanceTo(player.position) < 1) {
                player.damage(10);
            }
        });
    }

    checkKeyCollection(player) {
        this.keys.forEach(key => {
            if (!key.collected && 
                key.mesh.position.distanceTo(player.position) < 1) {
                key.collected = true;
                key.mesh.visible = false;
                document.getElementById('keys').textContent += 
                    (document.getElementById('keys').textContent ? ', ' : '') + 
                    key.color;
            }
        });
    }

    checkGateInteraction(player) {
        this.gates.forEach(gate => {
            if (!gate.opened && 
                gate.mesh.position.distanceTo(player.position) < 1) {
                const hasKey = this.keys.find(k => 
                    k.color === gate.color && k.collected);
                if (hasKey) {
                    gate.opened = true;
                    gate.mesh.visible = false;
                }
            }
        });
    }
}