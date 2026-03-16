import Fastify from 'fastify'
import notesRoutes from './routes/note.route'
import db from './db/schema.js'

const app = Fastify({ logger: true })

app.register(notesRoutes)

const start = async () => {
  try {
    const tableExists = db.prepare(
      "SELECT name FROM sqlite_master WHERE type='table' AND name='notes'"
    ).get()

    if (!tableExists) {
      app.log.error('Database table "notes" was not created')
      process.exit(1)
    }

    app.log.info('Database is ready')

    await app.listen({ port: 3000, host: '0.0.0.0' })
  } catch (err) {
    app.log.error(err)
    process.exit(1)
  }
}

start()