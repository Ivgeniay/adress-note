import db from '../db/schema'

interface Note {
  address: string
  building: string
  entrance: string
  note: string
  lat: number
  lng: number
}

const getAll = (): Note[] => {
  return db.prepare('SELECT * FROM notes').all() as Note[]
}

const getOne = (address: string, building: string, entrance: string): Note | undefined => {
  return db.prepare('SELECT * FROM notes WHERE address = ? AND building = ? AND entrance = ?')
    .get(address, building, entrance) as Note | undefined
}

const getNearby = (lat: number, lng: number, radius: number): Note[] => {
  const latDelta = radius / 111320
  const lngDelta = radius / (111320 * Math.cos(lat * Math.PI / 180))

  return db.prepare(`
    SELECT * FROM notes
    WHERE lat BETWEEN ? AND ?
    AND lng BETWEEN ? AND ?
  `).all(
    lat - latDelta,
    lat + latDelta,
    lng - lngDelta,
    lng + lngDelta
  ) as Note[]
}

const create = (data: Note): void => {
  db.prepare('INSERT INTO notes (address, building, entrance, note, lat, lng) VALUES (?, ?, ?, ?, ?, ?)')
    .run(data.address, data.building, data.entrance, data.note, data.lat, data.lng)
}

const update = (address: string, building: string, entrance: string, note: string): void => {
  db.prepare('UPDATE notes SET note = ? WHERE address = ? AND building = ? AND entrance = ?')
    .run(note, address, building, entrance)
}

const remove = (address: string, building: string, entrance: string): void => {
  db.prepare('DELETE FROM notes WHERE address = ? AND building = ? AND entrance = ?')
    .run(address, building, entrance)
}

export default { getAll, getOne, getNearby, create, update, remove }