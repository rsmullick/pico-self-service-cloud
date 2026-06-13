import { useState, useEffect, useCallback } from 'react'
import './index.css'
import { api, User, Plan, Resource, Invoice, ResourceEvent } from './api'

// ─── Helpers ────────────────────────────────────────────────────────────────

function Badge({ status }: { status: string }) {
  const s = status.toLowerCase()
  return <span className={`badge badge-${s}`}>{status}</span>
}

function Spinner() { return <span className="spinner" /> }

function fmt(n: number | string) {
  return Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function timeAgo(iso: string) {
  const diff = (Date.now() - new Date(iso).getTime()) / 1000
  if (diff < 60) return 'just now'
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  return `${Math.floor(diff / 86400)}d ago`
}

// ─── Login ───────────────────────────────────────────────────────────────────

function Login({ onLogin }: { onLogin: (u: User) => void }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const demoUsers = [
    { email: 'alice@pico.io', password: 'demo1234', label: 'Alice (User)' },
    { email: 'bob@pico.io', password: 'demo1234', label: 'Bob (User)' },
    { email: 'admin@pico.io', password: 'admin1234', label: 'Admin' },
  ]

  async function doLogin(e?: React.FormEvent, em?: string, pw?: string) {
    e?.preventDefault()
    const u = em ?? email; const p = pw ?? password
    if (!u || !p) return
    setLoading(true); setError('')
    try {
      const user = await api.login(u, p)
      onLogin(user)
    } catch (err: any) {
      setError(err.message || 'Login failed')
    } finally { setLoading(false) }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
      <div className="card" style={{ width: '100%', maxWidth: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{ fontSize: 32, marginBottom: 8 }}>☁️</div>
          <h1 style={{ fontSize: 22, fontWeight: 700, color: 'var(--text)' }}>PICO Cloud</h1>
          <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 4 }}>Self-Service Cloud Portal</p>
        </div>

        <form onSubmit={doLogin} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} type="email" />
          <input placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} type="password" />
          {error && <div className="error-banner">{error}</div>}
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? <Spinner /> : 'Sign In'}
          </button>
        </form>

        <div style={{ marginTop: 24, borderTop: '1px solid var(--border)', paddingTop: 16 }}>
          <p style={{ color: 'var(--text-dim)', fontSize: 12, marginBottom: 10, textAlign: 'center' }}>Quick access demo accounts</p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {demoUsers.map(u => (
              <button key={u.email} className="btn-ghost" style={{ textAlign: 'left', fontSize: 12 }}
                onClick={() => doLogin(undefined, u.email, u.password)}>
                {u.label} — {u.email}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

// ─── Nav ─────────────────────────────────────────────────────────────────────

type Tab = 'dashboard' | 'resources' | 'billing' | 'catalog'

function Nav({ user, tab, setTab, onLogout }: {
  user: User; tab: Tab; setTab: (t: Tab) => void; onLogout: () => void
}) {
  const tabs: { id: Tab; label: string; icon: string }[] = [
    { id: 'dashboard', label: 'Dashboard', icon: '⊞' },
    { id: 'catalog', label: 'Catalog', icon: '📦' },
    { id: 'resources', label: 'Resources', icon: '🖥' },
    { id: 'billing', label: 'Billing', icon: '💳' },
  ]
  return (
    <nav style={{
      background: 'var(--surface)', borderBottom: '1px solid var(--border)',
      display: 'flex', alignItems: 'center', padding: '0 24px', gap: 4, height: 52
    }}>
      <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--primary)', marginRight: 20 }}>☁️ PICO</div>
      {tabs.map(t => (
        <button key={t.id}
          onClick={() => setTab(t.id)}
          style={{
            background: tab === t.id ? 'var(--surface2)' : 'transparent',
            color: tab === t.id ? 'var(--text)' : 'var(--muted)',
            border: 'none', borderRadius: 6, padding: '6px 12px', fontSize: 13, fontWeight: 500,
            transition: 'all 0.15s', cursor: 'pointer'
          }}>
          {t.icon} {t.label}
        </button>
      ))}
      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 12 }}>
        <span style={{ fontSize: 12, color: 'var(--muted)' }}>
          {user.displayName} <Badge status={user.role} />
        </span>
        <button className="btn-ghost btn-sm" onClick={onLogout}>Sign out</button>
      </div>
    </nav>
  )
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

function Dashboard({ user }: { user: User }) {
  const [resources, setResources] = useState<Resource[]>([])
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.getResources(user.id),
      api.getInvoices(user.id)
    ]).then(([r, i]) => { setResources(r); setInvoices(i) })
      .finally(() => setLoading(false))
  }, [user.id])

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}><Spinner /></div>

  const running = resources.filter(r => r.status === 'RUNNING').length
  const total = resources.length
  const unpaid = invoices.filter(i => i.status === 'ISSUED').length
  const monthlySpend = resources.reduce((s, _) => s, 0)

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div>
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>Welcome back, {user.displayName}</h2>
        <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 2 }}>Here's what's happening with your infrastructure.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px,1fr))', gap: 14 }}>
        {[
          { label: 'Total Resources', value: total, icon: '🖥', color: 'var(--primary)' },
          { label: 'Running', value: running, icon: '🟢', color: 'var(--success)' },
          { label: 'Invoices Due', value: unpaid, icon: '📄', color: unpaid > 0 ? 'var(--warning)' : 'var(--success)' },
          { label: 'This Month Est.', value: `$${fmt(invoices.reduce((s, i) => s + Number(i.totalAmount), 0))}`, icon: '💰', color: 'var(--text)' },
        ].map(stat => (
          <div key={stat.label} className="card" style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <span style={{ fontSize: 22 }}>{stat.icon}</span>
            <span style={{ fontSize: 24, fontWeight: 700, color: stat.color }}>{stat.value}</span>
            <span style={{ fontSize: 12, color: 'var(--muted)' }}>{stat.label}</span>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div className="card">
          <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12, color: 'var(--muted)' }}>RECENT RESOURCES</h3>
          {resources.length === 0
            ? <p style={{ color: 'var(--text-dim)', fontSize: 13 }}>No resources yet.</p>
            : resources.slice(0, 4).map(r => (
              <div key={r.id} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '8px 0', borderBottom: '1px solid var(--border)'
              }}>
                <div>
                  <div style={{ fontWeight: 500, fontSize: 13 }}>{r.name}</div>
                  <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>{timeAgo(r.createdAt)}</div>
                </div>
                <Badge status={r.status} />
              </div>
            ))
          }
        </div>

        <div className="card">
          <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12, color: 'var(--muted)' }}>RECENT INVOICES</h3>
          {invoices.length === 0
            ? <p style={{ color: 'var(--text-dim)', fontSize: 13 }}>No invoices yet.</p>
            : invoices.slice(0, 4).map(inv => (
              <div key={inv.id} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '8px 0', borderBottom: '1px solid var(--border)'
              }}>
                <div>
                  <div style={{ fontWeight: 500, fontSize: 13 }}>${fmt(inv.totalAmount)}</div>
                  <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>{timeAgo(inv.createdAt)}</div>
                </div>
                <Badge status={inv.status} />
              </div>
            ))
          }
        </div>
      </div>
    </div>
  )
}

// ─── Catalog ─────────────────────────────────────────────────────────────────

function Catalog({ user, onProvision }: { user: User; onProvision: () => void }) {
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [selected, setSelected] = useState<Plan | null>(null)
  const [name, setName] = useState('')
  const [provisioning, setProvisioning] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    api.getPlans().then(p => setPlans(p)).finally(() => setLoading(false))
  }, [])

  async function doProvision() {
    if (!selected || !name.trim()) return
    setProvisioning(true); setError(''); setSuccess('')
    try {
      await api.createResource(user.id, selected.id, name.trim())
      setSuccess(`"${name}" is being provisioned! Check Resources tab.`)
      setName(''); setSelected(null)
      setTimeout(onProvision, 1500)
    } catch (err: any) { setError(err.message) }
    finally { setProvisioning(false) }
  }

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}><Spinner /></div>

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div>
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>Service Catalog</h2>
        <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 2 }}>Choose a VM plan to provision.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px,1fr))', gap: 14 }}>
        {plans.map(plan => (
          <div key={plan.id} className="card" onClick={() => { setSelected(plan); setName(''); setError(''); setSuccess('') }}
            style={{
              cursor: 'pointer', transition: 'all 0.15s',
              border: selected?.id === plan.id ? '1.5px solid var(--primary)' : '1px solid var(--border)',
              background: selected?.id === plan.id ? 'rgba(99,102,241,0.06)' : 'var(--surface)'
            }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
              <h3 style={{ fontWeight: 700, fontSize: 15 }}>{plan.name}</h3>
              <span style={{ fontWeight: 700, fontSize: 18, color: 'var(--primary)' }}>${fmt(plan.monthlyPrice)}<span style={{ fontSize: 11, fontWeight: 400, color: 'var(--muted)' }}>/mo</span></span>
            </div>
            <p style={{ fontSize: 12, color: 'var(--muted)', marginBottom: 14 }}>{plan.description}</p>
            <div style={{ display: 'flex', gap: 10, fontSize: 12, color: 'var(--text-dim)' }}>
              <span>🔲 {plan.cpu} vCPU</span>
              <span>💾 {plan.memoryGb}GB RAM</span>
              <span>💿 {plan.storageGb}GB SSD</span>
            </div>
          </div>
        ))}
      </div>

      {selected && (
        <div className="card" style={{ maxWidth: 500 }}>
          <h3 style={{ fontWeight: 600, marginBottom: 14 }}>Provision: {selected.name}</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div>
              <label style={{ fontSize: 12, color: 'var(--muted)', display: 'block', marginBottom: 5 }}>Resource Name</label>
              <input placeholder="e.g. web-server-prod" value={name} onChange={e => setName(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && doProvision()} />
            </div>
            <div style={{ background: 'var(--surface2)', borderRadius: 6, padding: 12, fontSize: 13 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--muted)' }}>
                <span>Estimated monthly cost</span>
                <span style={{ color: 'var(--text)', fontWeight: 600 }}>${fmt(selected.monthlyPrice)}</span>
              </div>
            </div>
            {error && <div className="error-banner">{error}</div>}
            {success && <div style={{ background: 'rgba(34,197,94,0.1)', border: '1px solid rgba(34,197,94,0.3)', color: '#86efac', padding: '10px 14px', borderRadius: 6, fontSize: 13 }}>{success}</div>}
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn-primary" onClick={doProvision} disabled={provisioning || !name.trim()}>
                {provisioning ? <Spinner /> : '🚀 Provision VM'}
              </button>
              <button className="btn-ghost" onClick={() => setSelected(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Resources ───────────────────────────────────────────────────────────────

function ResourceDetail({ resource, user, onBack, onUpdate }: {
  resource: Resource; user: User; onBack: () => void; onUpdate: () => void
}) {
  const [events, setEvents] = useState<ResourceEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [actioning, setActioning] = useState(false)
  const [error, setError] = useState('')

  const loadEvents = useCallback(() => {
    api.getResourceEvents(resource.id).then(setEvents).finally(() => setLoading(false))
  }, [resource.id])

  useEffect(() => { loadEvents() }, [loadEvents])

  async function doAction(action: string) {
    setActioning(true); setError('')
    try {
      await api.resourceAction(resource.id, action, user.id)
      onUpdate()
    } catch (err: any) { setError(err.message) }
    finally { setActioning(false) }
  }

  const actions: Record<string, { label: string; cls: string }[]> = {
    RUNNING: [{ label: 'Stop', cls: 'btn-ghost' }, { label: 'Terminate', cls: 'btn-danger' }],
    STOPPED: [{ label: 'Start', cls: 'btn-success' }, { label: 'Terminate', cls: 'btn-danger' }],
    PENDING: [], PROVISIONING: [], FAILED: [], TERMINATED: [],
  }

  const availableActions = actions[resource.status] || []

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <button className="btn-ghost btn-sm" onClick={onBack}>← Back</button>
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>{resource.name}</h2>
        <Badge status={resource.status} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div className="card">
          <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--muted)', marginBottom: 12 }}>RESOURCE DETAILS</h3>
          {[
            ['ID', resource.id.substring(0, 16) + '...'],
            ['Type', resource.resourceType],
            ['External ID', resource.externalResourceId || '—'],
            ['Plan', resource.planId.substring(0, 8) + '...'],
            ['Created', new Date(resource.createdAt).toLocaleString()],
          ].map(([k, v]) => (
            <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border)', fontSize: 13 }}>
              <span style={{ color: 'var(--muted)' }}>{k}</span>
              <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{v}</span>
            </div>
          ))}

          <div style={{ marginTop: 16, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {availableActions.map(a => (
              <button key={a.label} className={a.cls} onClick={() => doAction(a.label.toUpperCase())} disabled={actioning}>
                {actioning ? <Spinner /> : a.label}
              </button>
            ))}
          </div>
          {error && <div className="error-banner" style={{ marginTop: 10 }}>{error}</div>}
        </div>

        <div className="card">
          <h3 style={{ fontSize: 13, fontWeight: 600, color: 'var(--muted)', marginBottom: 12 }}>TIMELINE</h3>
          {loading ? <Spinner /> : events.length === 0 ? (
            <p style={{ color: 'var(--text-dim)', fontSize: 13 }}>No events yet.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
              {events.map((ev, i) => (
                <div key={ev.id} style={{ display: 'flex', gap: 10, paddingBottom: 12, position: 'relative' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                    <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--primary)', marginTop: 4, flexShrink: 0 }} />
                    {i < events.length - 1 && <div style={{ width: 1, flex: 1, background: 'var(--border)', marginTop: 4 }} />}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text)' }}>{ev.eventType}</div>
                    {ev.details && <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 1 }}>{ev.details}</div>}
                    <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 2 }}>{timeAgo(ev.createdAt)}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function Resources({ user }: { user: User }) {
  const [resources, setResources] = useState<Resource[]>([])
  const [loading, setLoading] = useState(true)
  const [selected, setSelected] = useState<Resource | null>(null)
  const [filter, setFilter] = useState('ALL')

  const load = useCallback(() => {
    setLoading(true)
    api.getResources(user.id).then(setResources).finally(() => setLoading(false))
  }, [user.id])

  useEffect(() => { load() }, [load])

  // Auto-refresh for pending/provisioning resources
  useEffect(() => {
    const hasPending = resources.some(r => ['PENDING', 'PROVISIONING'].includes(r.status))
    if (!hasPending) return
    const t = setTimeout(load, 3000)
    return () => clearTimeout(t)
  }, [resources, load])

  const statuses = ['ALL', 'RUNNING', 'STOPPED', 'PENDING', 'PROVISIONING', 'FAILED', 'TERMINATED']
  const filtered = filter === 'ALL' ? resources : resources.filter(r => r.status === filter)

  if (selected) {
    const fresh = resources.find(r => r.id === selected.id) || selected
    return <ResourceDetail resource={fresh} user={user} onBack={() => setSelected(null)}
      onUpdate={() => { load(); setSelected(null) }} />
  }

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>My Resources</h2>
          <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 2 }}>{resources.length} resource{resources.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn-ghost btn-sm" onClick={load}>↻ Refresh</button>
      </div>

      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {statuses.map(s => (
          <button key={s} onClick={() => setFilter(s)}
            style={{
              background: filter === s ? 'var(--primary)' : 'var(--surface2)',
              color: filter === s ? 'white' : 'var(--muted)',
              border: '1px solid var(--border)', borderRadius: 6,
              padding: '4px 10px', fontSize: 11, fontWeight: 600, cursor: 'pointer'
            }}>{s}</button>
        ))}
      </div>

      {loading ? (
        <div style={{ padding: 40, textAlign: 'center' }}><Spinner /></div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <div style={{ fontSize: 40 }}>🖥</div>
          <h3 style={{ marginTop: 10 }}>No resources</h3>
          <p>Provision a VM from the Catalog tab.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px,1fr))', gap: 12 }}>
          {filtered.map(r => (
            <div key={r.id} className="card" onClick={() => setSelected(r)}
              style={{ cursor: 'pointer', transition: 'border-color 0.15s' }}
              onMouseEnter={e => (e.currentTarget.style.borderColor = 'var(--primary)')}
              onMouseLeave={e => (e.currentTarget.style.borderColor = 'var(--border)')}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                <h3 style={{ fontWeight: 600, fontSize: 14 }}>{r.name}</h3>
                <Badge status={r.status} />
              </div>
              <div style={{ fontSize: 12, color: 'var(--text-dim)', display: 'flex', flexDirection: 'column', gap: 3 }}>
                <span>Type: {r.resourceType}</span>
                {r.externalResourceId && <span style={{ fontFamily: 'monospace' }}>ID: {r.externalResourceId}</span>}
                <span>Created {timeAgo(r.createdAt)}</span>
              </div>
              {['PENDING', 'PROVISIONING'].includes(r.status) && (
                <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--warning)' }}>
                  <Spinner /> Provisioning...
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ─── Billing ─────────────────────────────────────────────────────────────────

function Billing({ user }: { user: User }) {
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [paying, setPaying] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState<string | null>(null)

  const load = useCallback(() => {
    api.getInvoices(user.id).then(setInvoices).finally(() => setLoading(false))
  }, [user.id])

  useEffect(() => { load() }, [load])

  async function generateInvoice() {
    setGenerating(true); setError('')
    try { await api.generateInvoice(user.id, user.id); load() }
    catch (err: any) { setError(err.message) }
    finally { setGenerating(false) }
  }

  async function payInvoice(id: string) {
    setPaying(id); setError('')
    try { await api.payInvoice(id, user.id); load() }
    catch (err: any) { setError(err.message) }
    finally { setPaying(null) }
  }

  const totalDue = invoices.filter(i => i.status === 'ISSUED').reduce((s, i) => s + Number(i.totalAmount), 0)

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>Billing & Invoices</h2>
          {totalDue > 0 && <p style={{ color: 'var(--warning)', fontSize: 13, marginTop: 2 }}>${fmt(totalDue)} outstanding</p>}
        </div>
        <button className="btn-primary" onClick={generateInvoice} disabled={generating}>
          {generating ? <Spinner /> : '+ Generate Invoice'}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div style={{ padding: 40, textAlign: 'center' }}><Spinner /></div>
      ) : invoices.length === 0 ? (
        <div className="empty-state">
          <div style={{ fontSize: 40 }}>💳</div>
          <h3 style={{ marginTop: 10 }}>No invoices</h3>
          <p>Generate your first invoice when you have active resources.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {invoices.map(inv => (
            <div key={inv.id} className="card">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}
                onClick={() => setExpanded(expanded === inv.id ? null : inv.id)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 15 }}>${fmt(inv.totalAmount)}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-dim)', marginTop: 1 }}>{new Date(inv.createdAt).toLocaleDateString()} · {inv.items.length} item{inv.items.length !== 1 ? 's' : ''}</div>
                  </div>
                  <Badge status={inv.status} />
                </div>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  {inv.status === 'ISSUED' && (
                    <button className="btn-success btn-sm" onClick={e => { e.stopPropagation(); payInvoice(inv.id) }} disabled={paying === inv.id}>
                      {paying === inv.id ? <Spinner /> : '✓ Mark Paid'}
                    </button>
                  )}
                  <span style={{ color: 'var(--muted)', fontSize: 16 }}>{expanded === inv.id ? '▲' : '▼'}</span>
                </div>
              </div>

              {expanded === inv.id && (
                <div style={{ marginTop: 14, paddingTop: 14, borderTop: '1px solid var(--border)' }}>
                  {inv.items.map(item => (
                    <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border)', fontSize: 13 }}>
                      <span style={{ color: 'var(--muted)' }}>{item.description}</span>
                      <span style={{ fontWeight: 600 }}>${fmt(item.amount)}</span>
                    </div>
                  ))}
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0 0', fontSize: 14, fontWeight: 700 }}>
                    <span>Total</span>
                    <span style={{ color: 'var(--primary)' }}>${fmt(inv.totalAmount)}</span>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ─── App Shell ───────────────────────────────────────────────────────────────

export default function App() {
  const [user, setUser] = useState<User | null>(() => {
    try { return JSON.parse(localStorage.getItem('pico_user') || 'null') } catch { return null }
  })
  const [tab, setTab] = useState<Tab>('dashboard')

  function login(u: User) {
    localStorage.setItem('pico_user', JSON.stringify(u))
    setUser(u)
  }
  function logout() {
    localStorage.removeItem('pico_user')
    setUser(null)
  }

  if (!user) return <Login onLogin={login} />

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Nav user={user} tab={tab} setTab={setTab} onLogout={logout} />
      <main style={{ flex: 1, maxWidth: 1100, width: '100%', margin: '0 auto', width: '100%' }}>
        {tab === 'dashboard' && <Dashboard user={user} />}
        {tab === 'catalog' && <Catalog user={user} onProvision={() => setTab('resources')} />}
        {tab === 'resources' && <Resources user={user} />}
        {tab === 'billing' && <Billing user={user} />}
      </main>
    </div>
  )
}
