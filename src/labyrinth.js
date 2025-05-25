import * as THREE from 'three';
import { Cell } from './cell.js';
import { TextureLoader } from './textureLoader.js';

export class Labyrinth {
    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.cells = [];
        this.monsters = [];
        this.keys = [];
        this.gates = [];
        this.textureLoader = new TextureLoader();
        this.textures = null;
    }

    async initialize() {
        this.textures = await this.textureLoader.loadTextures();
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

        directions.sort(() => Math.random() - 0.5);

        this.cells[x][z].visited = true;

        for (const [dx, dz] of directions) {
            const newX = x + dx;
            const newZ = z + dz;
            
            if (this.isValidCell(newX, newZ) && !this.cells[newX][newZ].visited) {
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
            const keyPos = this.getRandomEmptyCell();
            this.keys.push({
                position: keyPos,
                color: color,
                collected: false
            });

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

    async build(scene) {
        await this.initialize();

        // Create geometries
        const wallGeometry = new THREE.BoxGeometry(1, 2, 1);
        const wallMaterial = new THREE.MeshPhongMaterial({ 
            map: this.textures.redWall,
            bumpMap: this.textures.redWall,
            bumpScale: 0.1
        });

        const floorMaterial = new THREE.MeshPhongMaterial({ 
            map: this.textures.redFloor,
            bumpMap: this.textures.redFloor,
            bumpScale: 0.1
        });

        // Build maze
        for (let x = 0; x < this.width; x++) {
            for (let z = 0; z < this.height; z++) {
                const cell = this.cells[x][z];
                if (cell.type === Cell.WALL) {
                    const wall = new THREE.Mesh(wallGeometry, wallMaterial.clone());
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
        await this.buildKeysAndGates(scene);
    }

    async buildKeysAndGates(scene) {
        const keyGeometry = new THREE.BoxGeometry(0.3, 0.3, 0.3);
        const gateGeometry = new THREE.BoxGeometry(1, 2, 0.1);

        const keyMaterials = {
            red: new THREE.MeshPhongMaterial({ map: this.textures.redAltar }),
            blue: new THREE.MeshPhongMaterial({ map: this.textures.blueAltar }),
            yellow: new THREE.MeshPhongMaterial({ map: this.textures.redAltar, color: 0xffff00 })
        };

        const gateMaterials = {
            red: new THREE.MeshPhongMaterial({ map: this.textures.redWall }),
            blue: new THREE.MeshPhongMaterial({ map: this.textures.blueWall }),
            yellow: new THREE.MeshPhongMaterial({ map: this.textures.redWall, color: 0xffff00 })
        };

        this.keys.forEach(key => {
            const material = keyMaterials[key.color];
            const mesh = new THREE.Mesh(keyGeometry, material);
            mesh.position.set(key.position.x, 0.5, key.position.z);
            scene.add(mesh);
            key.mesh = mesh;
        });

        this.gates.forEach(gate => {
            const material = gateMaterials[gate.color].clone();
            material.transparent = true;
            material.opacity = 0.9;
            const mesh = new THREE.Mesh(gateGeometry, material);
            mesh.position.set(gate.position.x, 1, gate.position.z);
            scene.add(mesh);
            gate.mesh = mesh;
        });
    }

    update(player) {
        this.updateMonsters(player);
        this.checkKeyCollection(player);
        this.checkGateInteraction(player);
    }

    updateMonsters(player) {
        this.monsters.forEach(monster => {
            if (monster.health <= 0) return;

            const direction = new THREE.Vector3()
                .subVectors(player.position, monster.position)
                .normalize();
            monster.position.add(direction.multiplyScalar(0.05));

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