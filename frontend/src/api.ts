const BASE = '/api'

export interface User { id: string; email: string; displayName: string; role: 'ADMIN' | 'USER' }
export interface Plan { id: string; name: string; description: string; cpu: number; memoryGb: number; storageGb: number; monthlyPrice: number; createdAt: string }
export interface Resource { id: string; customerId: string; name: string; status: string; resourceType: string; planId: string; externalResourceId: string | null; createdAt: string }
export interface ResourceEvent { id: string; resourceId: string; eventType: string; details: string; createdAt: string }
export interface InvoiceItem { id: string; description: string; amount: number }
export interface Invoice { id: string; customerId: string; status: string; totalAmount: number; createdAt: string; items: InvoiceItem[] }

async function req<T>(path: string, opts?: RequestInit): Promise<T> {
  const r = await fetch(BASE + path, { headers: { 'Content-Type': 'application/json' }, ...opts })
  if (!r.ok) {
    const body = await r.json().catch(() => ({}))
    throw new Error(body.detail || body.message || `HTTP ${r.status}`)
  }
  if (r.status === 204) return undefined as T
  return r.json()
}

export const api = {
  login: (email: string, password: string) => req<User>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  getPlans: () => req<Plan[]>('/plans'),
  getResources: (customerId: string) => req<Resource[]>(`/resources?customerId=${customerId}`),
  createResource: (customerId: string, planId: string, resourceName: string) =>
    req<Resource>('/resources', { method: 'POST', body: JSON.stringify({ customerId, planId, resourceName }) }),
  resourceAction: (id: string, action: string, actorId: string) =>
    req<Resource>(`/resources/${id}/actions`, { method: 'POST', body: JSON.stringify({ action, actorId }) }),
  getResourceEvents: (id: string) => req<ResourceEvent[]>(`/resources/${id}/events`),
  getInvoices: (customerId: string) => req<Invoice[]>(`/invoices?customerId=${customerId}`),
  generateInvoice: (customerId: string, actorId: string) =>
    req<Invoice>('/invoices/generate', { method: 'POST', body: JSON.stringify({ customerId, actorId }) }),
  payInvoice: (id: string, actorId: string) =>
    req<Invoice>(`/invoices/${id}/pay`, { method: 'POST', body: JSON.stringify({ actorId }) }),
}
