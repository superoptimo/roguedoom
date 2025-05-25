export class Cell {
    static WALL = 1;
    static FLOOR = 0;

    constructor(x, z) {
        this.x = x;
        this.z = z;
        this.type = Cell.WALL;
        this.visited = false;
    }
}