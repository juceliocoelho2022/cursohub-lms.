export type Role = 'STUDENT' | 'INSTRUCTOR' | 'ADMIN'

export interface AuthSession {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  userId: string
  name: string
  email: string
  role: Role
}

export interface CurrentUser {
  id: string
  name: string
  email: string
  role: Role
}

async function authRequest(url: string, body: unknown): Promise<AuthSession> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    credentials: 'omit',
  })
  if (!response.ok) {
    let message = 'Não foi possível concluir sua solicitação. Tente novamente.'
    try {
      const data = (await response.json()) as { message?: string }
      if (data.message) message = data.message
    } catch { /* A API pode retornar corpo vazio. */ }
    throw new Error(message)
  }
  return (await response.json()) as AuthSession
}

export function registerStudent(name: string, email: string, password: string) {
  // O backend atribui STUDENT: o cliente nunca envia perfil ou privilégios.
  return authRequest('/api/v1/auth/register', { name, email, password })
}

export function login(email: string, password: string) {
  return authRequest('/api/v1/auth/login', { email, password })
}

export async function currentUser(token: string): Promise<CurrentUser> {
  const response = await fetch('/api/v1/auth/me', {
    headers: { Authorization: `Bearer ${token}` },
    credentials: 'omit',
  })
  if (!response.ok) throw new Error('Sessão inválida ou expirada. Entre novamente.')
  return (await response.json()) as CurrentUser
}

// Token guardado apenas no estado React (memória), sem localStorage/sessionStorage.
