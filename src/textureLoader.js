import * as THREE from 'three';

export class TextureLoader {
    constructor() {
        this.loader = new THREE.TextureLoader();
        this.textures = {};
    }

    async loadTextures() {
        const textureFiles = {
            redWall: '/roguedoom_classic/images/REDWALL.gif',
            blueWall: '/roguedoom_classic/images/BLUEWALL.gif',
            redFloor: '/roguedoom_classic/images/REDFLOOR.gif',
            blueFloor: '/roguedoom_classic/images/BLUEFLOOR.gif',
            redAltar: '/roguedoom_classic/images/REDALTAR.gif',
            blueAltar: '/roguedoom_classic/images/BLUEALTAR.gif',
            redBase: '/roguedoom_classic/images/REDBASEWALL.gif',
            blueBase: '/roguedoom_classic/images/BLUEBASEWALL.gif',
            elevator: '/roguedoom_classic/images/ELEVATORROOM.gif'
        };

        for (const [key, path] of Object.entries(textureFiles)) {
            this.textures[key] = await this.loadTexture(path);
        }

        return this.textures;
    }

    loadTexture(path) {
        return new Promise((resolve, reject) => {
            this.loader.load(
                path,
                (texture) => {
                    texture.wrapS = texture.wrapT = THREE.RepeatWrapping;
                    texture.repeat.set(1, 1);
                    resolve(texture);
                },
                undefined,
                (error) => reject(error)
            );
        });
    }
}