import Database from 'better-sqlite3'

const db = new Database('address_notes.db')

db.pragma('journal_mode = WAL')

db.exec(`
  CREATE TABLE IF NOT EXISTS notes (
    address  TEXT NOT NULL,
    building TEXT NOT NULL,
    entrance TEXT NOT NULL,
    note     TEXT NOT NULL,
    PRIMARY KEY (address, building, entrance)
  )
`)

export default db