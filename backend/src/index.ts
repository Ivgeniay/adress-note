import Fastify from 'fastify'
import cors from '@fastify/cors'
import db from './db/schema.js'
import notesRoutes from './routes/note.route.js'

const app = Fastify({ logger: true })


const start = async () => {
  await app.register(cors, {
    origin: true
  })

  app.register(notesRoutes)

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