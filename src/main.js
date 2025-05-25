import * as THREE from 'three';
import { PointerLockControls } from 'three/examples/jsm/controls/PointerLockControls';
import { Labyrinth } from './labyrinth.js';
import { Player } from './player.js';

class Game {
    constructor() {
        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.shadowMap.enabled = true;
        document.body.appendChild(this.renderer.domElement);

        // Lighting
        this.setupLighting();

        // Initialize labyrinth
        this.labyrinth = new Labyrinth(20, 20); // 20x20 map size
        this.labyrinth.generate();
        this.labyrinth.build(this.scene);

        // Initialize player
        this.player = new Player(this.camera);
        this.controls = new PointerLockControls(this.camera, document.body);

        // Event listeners
        this.setupEventListeners();

        // Start game loop
        this.animate();
    }

    setupLighting() {
        const ambientLight = new THREE.AmbientLight(0x404040);
        this.scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
        directionalLight.position.set(5, 5, 5);
        directionalLight.castShadow = true;
        this.scene.add(directionalLight);
    }

    setupEventListeners() {
        window.addEventListener('resize', () => {
            this.camera.aspect = window.innerWidth / window.innerHeight;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(window.innerWidth, window.innerHeight);
        });

        document.addEventListener('click', () => {
            this.controls.lock();
        });

        document.addEventListener('keydown', (e) => this.player.handleKeyDown(e));
        document.addEventListener('keyup', (e) => this.player.handleKeyUp(e));
    }

    animate() {
        requestAnimationFrame(() => this.animate());

        if (this.controls.isLocked) {
            this.player.update(this.labyrinth);
            this.labyrinth.update(this.player);
        }

        this.renderer.render(this.scene, this.camera);
    }
}

// Start the game
new Game();