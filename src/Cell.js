export class Cell {
    // Cell types
    static NORMALROOM = 0;
    static REDBASE = 1;
    static BLUEBASE = 2;
    static ELEVATOR = 3; // The moving platform cell
    static ELEVATORROOM = 4; // Cells surrounding the elevator platform
    static OUTSIDE = 5;
    static REDALTAR = 6;
    static BLUEALTAR = 7;
    static REDSOURCE = 8;
    static BLUESOURCE = 9;
    // Add more as needed

    constructor(row, column, upstairs) {
        this.EastBound = false; // true if there is a wall to the east
        this.SouthBound = false; // true if there is a wall to the south

        this.mark = Cell.NORMALROOM; // type of cell
        this.m_row = row;
        this.m_column = column;
        this.m_upstairs = upstairs; // true if the cell is on the upper floor

        this.m_entities = []; // list of 3D objects in this cell

        // For visibility determination
        this.leftTangent = 0.0;
        this.rightTangent = 0.0;
        this.mark2 = false; // used by visibility algorithms
    }
}
