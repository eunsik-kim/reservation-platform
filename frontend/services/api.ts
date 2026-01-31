import type {
  LoginRequest,
  SignupRequest,
  TokenResponse,
  User,
  Event,
  EventListResponse,
  CreateEventRequest,
  UpdateEventRequest,
  Slot,
  CreateSlotRequest,
  Reservation,
  ReservationListResponse,
  CreateReservationRequest,
  QueuePosition,
  Payment,
  CreatePaymentRequest,
  Participant,
  ParticipantListResponse
} from '~/types'

const API_BASE_URL = 'http://localhost:8080/api/v1'

interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: {
    code: string
    message: string
  }
}

class ApiService {
  private getToken(): string | null {
    if (process.client) {
      return localStorage.getItem('accessToken')
    }
    return null
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = this.getToken()

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers
    })

    const result: ApiResponse<T> = await response.json()

    if (!response.ok || !result.success) {
      throw new Error(result.error?.message || 'API 요청에 실패했습니다')
    }

    return result.data as T
  }

  // Auth
  async login(data: LoginRequest): Promise<TokenResponse> {
    return this.request<TokenResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async signup(data: SignupRequest): Promise<User> {
    return this.request<User>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async logout(): Promise<void> {
    const token = this.getToken()
    if (token) {
      await this.request<void>('/auth/logout', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
    }
  }

  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    return this.request<TokenResponse>('/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken })
    })
  }

  async getCurrentUser(): Promise<User> {
    return this.request<User>('/auth/me')
  }

  // Events
  async getEvents(page = 0, size = 20): Promise<EventListResponse> {
    return this.request<EventListResponse>(`/events?page=${page}&size=${size}`)
  }

  async getEvent(id: number): Promise<Event> {
    return this.request<Event>(`/events/${id}`)
  }

  async getMyEvents(page = 0, size = 20): Promise<EventListResponse> {
    return this.request<EventListResponse>(`/events/my?page=${page}&size=${size}`)
  }

  async createEvent(data: CreateEventRequest): Promise<Event> {
    return this.request<Event>('/events', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async updateEvent(id: number, data: UpdateEventRequest): Promise<Event> {
    return this.request<Event>(`/events/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  }

  async deleteEvent(id: number): Promise<void> {
    return this.request<void>(`/events/${id}`, {
      method: 'DELETE'
    })
  }

  async publishEvent(id: number): Promise<Event> {
    return this.request<Event>(`/events/${id}/publish`, {
      method: 'POST'
    })
  }

  async getSlots(eventId: number): Promise<Slot[]> {
    return this.request<Slot[]>(`/events/${eventId}/slots`)
  }

  async addSlot(eventId: number, data: CreateSlotRequest): Promise<Slot> {
    return this.request<Slot>(`/events/${eventId}/slots`, {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async getParticipants(eventId: number, page = 0, size = 20): Promise<ParticipantListResponse> {
    return this.request<ParticipantListResponse>(`/events/${eventId}/participants?page=${page}&size=${size}`)
  }

  // Queue
  async enterQueue(eventId: number): Promise<QueuePosition> {
    return this.request<QueuePosition>('/queue/enter', {
      method: 'POST',
      body: JSON.stringify({ eventId })
    })
  }

  async getQueuePosition(eventId: number): Promise<QueuePosition> {
    return this.request<QueuePosition>(`/queue/position?eventId=${eventId}`)
  }

  async leaveQueue(eventId: number): Promise<boolean> {
    return this.request<boolean>(`/queue/leave?eventId=${eventId}`, {
      method: 'DELETE'
    })
  }

  // Reservations
  async getMyReservations(page = 0, size = 20): Promise<ReservationListResponse> {
    return this.request<ReservationListResponse>(`/reservations/my?page=${page}&size=${size}`)
  }

  async getReservation(id: number): Promise<Reservation> {
    return this.request<Reservation>(`/reservations/${id}`)
  }

  async createReservation(data: CreateReservationRequest): Promise<Reservation> {
    return this.request<Reservation>('/reservations', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async cancelReservation(id: number): Promise<void> {
    return this.request<void>(`/reservations/${id}`, {
      method: 'DELETE'
    })
  }

  // Payments
  async createPayment(data: CreatePaymentRequest): Promise<Payment> {
    return this.request<Payment>('/payments', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }

  async getPayment(id: number): Promise<Payment> {
    return this.request<Payment>(`/payments/${id}`)
  }

  async getPaymentByReservation(reservationId: number): Promise<Payment> {
    return this.request<Payment>(`/payments/reservation/${reservationId}`)
  }
}

export const api = new ApiService()
