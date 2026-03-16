import { FastifyInstance } from 'fastify'
import notesService from '../services/notes.service'

export default async function notesRoutes(app: FastifyInstance) {
  app.get('/notes', async () => {
    return notesService.getAll()
  })

  app.get<{ Params: { address: string; building: string; entrance: string } }>(
    '/notes/:address/:building/:entrance',
    async (request, reply) => {
      const { address, building, entrance } = request.params
      const note = notesService.getOne(address, building, entrance)
      if (!note) return reply.status(404).send({ error: 'Not found' })
      return note
    }
  )

  app.post<{ Body: { address: string; building: string; entrance: string; note: string; lat: number; lng: number } }>(
    '/notes',
    async (request, reply) => {
      notesService.create(request.body)
      return reply.status(201).send({ ok: true })
    }
  )

  app.put<{
    Params: { address: string; building: string; entrance: string }
    Body: { note: string }
  }>(
    '/notes/:address/:building/:entrance',
    async (request, reply) => {
      const { address, building, entrance } = request.params
      notesService.update(address, building, entrance, request.body.note)
      return reply.send({ ok: true })
    }
  )

  app.delete<{ Params: { address: string; building: string; entrance: string } }>(
    '/notes/:address/:building/:entrance',
    async (request, reply) => {
      const { address, building, entrance } = request.params
      notesService.remove(address, building, entrance)
      return reply.send({ ok: true })
    }
  )
}