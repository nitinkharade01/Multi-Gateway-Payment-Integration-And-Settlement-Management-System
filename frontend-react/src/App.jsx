import { useEffect, useMemo, useState } from "react";
import { Link, NavLink, Navigate, Route, Routes, useNavigate, useParams } from "react-router-dom";
import {
  Activity, BadgeIndianRupee, Building2, CircleCheck, CircleX, CreditCard, FileSearch,
  HeartPulse, KeyRound, LogOut, ReceiptIndianRupee, RefreshCcw, ShieldAlert, Webhook
} from "lucide-react";
import { api, problem } from "./api";

const merchantLinks = [
  ["/merchant/dashboard", "Dashboard", Activity],
  ["/merchant/api-keys", "API Keys", KeyRound],
  ["/merchant/orders", "Create Order", CreditCard],
  ["/merchant/payments", "Payment History", ReceiptIndianRupee],
  ["/merchant/refunds", "Refunds", RefreshCcw],
  ["/merchant/settlements", "Settlements", BadgeIndianRupee],
  ["/merchant/webhooks", "Webhook", Webhook]
];

const adminLinks = [
  ["/admin/dashboard", "Admin Dashboard", Activity],
  ["/admin/merchants", "Merchants", Building2],
  ["/admin/transactions", "Transactions", ReceiptIndianRupee],
  ["/admin/gateways", "Gateway Health", HeartPulse],
  ["/admin/refunds", "Refund Requests", RefreshCcw],
  ["/admin/settlements", "Settlement Batches", BadgeIndianRupee],
  ["/admin/reconciliation", "Reconciliation", FileSearch],
  ["/admin/fraud", "Fraud Alerts", ShieldAlert]
];

const defaultMerchant = () => sessionStorage.getItem("merchantId") || "";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/checkout/:orderId" element={<Checkout />} />
      <Route path="/checkout/success/:transactionId" element={<CheckoutResult success />} />
      <Route path="/checkout/failure/:transactionId" element={<CheckoutResult />} />
      <Route path="/checkout/status/:transactionId" element={<StatusPage />} />
      <Route path="/merchant/*" element={<PortalShell links={merchantLinks} title="Merchant Portal"><MerchantRoutes /></PortalShell>} />
      <Route path="/admin/*" element={<PortalShell links={adminLinks} title="Admin Portal"><AdminRoutes /></PortalShell>} />
      <Route path="*" element={<Navigate to="/merchant/dashboard" replace />} />
    </Routes>
  );
}

function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const request = useRequest();
  async function submit(event) {
    event.preventDefault();
    const response = await request.run(() => api.post("/api/auth/login", form));
    if (response) {
      sessionStorage.setItem("accessToken", response.accessToken);
      sessionStorage.setItem("profile", JSON.stringify(response.profile));
      navigate(response.profile.role === "ADMIN" ? "/admin/dashboard" : "/merchant/dashboard");
    }
  }
  return (
    <main className="login-stage">
      <form className="login-panel" onSubmit={submit}>
        <Brand />
        <h1>Sign in</h1>
        <TextField label="Email" value={form.email} onChange={(email) => setForm({ ...form, email })} type="email" />
        <TextField label="Password" value={form.password} onChange={(password) => setForm({ ...form, password })} type="password" />
        <button className="btn btn-dark w-100" disabled={request.busy}>Continue</button>
        <Notice request={request} />
      </form>
    </main>
  );
}

function PortalShell({ links, title, children }) {
  const navigate = useNavigate();
  const profile = JSON.parse(sessionStorage.getItem("profile") || "{}");
  return (
    <div className="portal">
      <aside className="portal-nav">
        <Brand />
        <nav>
          {links.map(([to, label, Icon]) => (
            <NavLink key={to} to={to} className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}>
              <Icon size={18} /><span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <section className="portal-main">
        <header className="portal-topbar">
          <div><small>{title}</small><strong>{profile.fullName || profile.email || "Operations"}</strong></div>
          <button className="icon-button" title="Sign out" onClick={() => { sessionStorage.clear(); navigate("/login"); }}>
            <LogOut size={18} />
          </button>
        </header>
        <div className="workspace">{children}</div>
      </section>
    </div>
  );
}

function MerchantRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<MerchantDashboard />} />
      <Route path="api-keys" element={<ApiKeys />} />
      <Route path="orders" element={<CreateOrder />} />
      <Route path="payments" element={<PaymentHistory />} />
      <Route path="refunds" element={<RefundManager />} />
      <Route path="settlements" element={<Settlements />} />
      <Route path="webhooks" element={<WebhookConfig />} />
      <Route path="*" element={<Navigate to="dashboard" replace />} />
    </Routes>
  );
}

function AdminRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<AdminDashboard />} />
      <Route path="merchants" element={<MerchantManager />} />
      <Route path="transactions" element={<TransactionMonitor />} />
      <Route path="gateways" element={<GatewayHealth />} />
      <Route path="refunds" element={<RefundManager />} />
      <Route path="settlements" element={<Settlements />} />
      <Route path="reconciliation" element={<Reconciliation />} />
      <Route path="fraud" element={<FraudConsole />} />
      <Route path="*" element={<Navigate to="dashboard" replace />} />
    </Routes>
  );
}

function MerchantDashboard() {
  const [merchantId, setMerchantId] = useMerchantId();
  const request = useRequest();
  const orders = request.data?.content || [];
  useEffect(() => { if (merchantId) request.run(() => api.get(`/api/payments/merchant/${merchantId}?size=5&sort=createdAt,desc`)); }, [merchantId]);
  return (
    <Page title="Merchant Dashboard" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <div className="metric-grid">
        <Metric label="Recent orders" value={orders.length} />
        <Metric label="Pending checkout" value={orders.filter((order) => order.status === "PENDING").length} />
        <Metric label="Captured" value={orders.filter((order) => order.status === "SUCCESS").length} />
      </div>
      <DataPanel title="Latest orders"><OrderTable orders={orders} /></DataPanel>
      <Notice request={request} />
    </Page>
  );
}

function ApiKeys() {
  const [merchantId, setMerchantId] = useMerchantId();
  const request = useRequest();
  return (
    <Page title="API Keys" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <button className="btn btn-dark" disabled={!merchantId || request.busy}
              onClick={() => request.run(() => api.post(`/api/merchants/${merchantId}/api-key`))}>
        Rotate API key
      </button>
      {request.data && <DataPanel title="New credential">
        <KeyValue label="API key" value={request.data.apiKey} />
        <KeyValue label="API secret" value={request.data.apiSecret} />
        <KeyValue label="Expires" value={request.data.expiresAt} />
      </DataPanel>}
      <Notice request={request} />
    </Page>
  );
}

function CreateOrder() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    merchantId: defaultMerchant(), apiKey: "", apiSecret: "", idempotencyKey: crypto.randomUUID(),
    amount: "1250.00", currency: "INR", paymentMode: "UPI", customerEmail: "", customerPhone: "",
    returnUrl: "http://localhost:5173/checkout/status/demo"
  });
  const request = useRequest();
  async function submit(event) {
    event.preventDefault();
    const response = await request.run(() => api.post("/api/payments/orders", { ...form, amount: Number(form.amount) }));
    if (response) navigate(`/checkout/${response.orderId}`, { state: response });
  }
  return (
    <Page title="Create Payment Order">
      <form className="form-grid" onSubmit={submit}>
        <TextField label="Merchant ID" value={form.merchantId} onChange={(merchantId) => updateAndRemember(setForm, form, merchantId)} />
        <TextField label="Idempotency key" value={form.idempotencyKey} onChange={(idempotencyKey) => setForm({ ...form, idempotencyKey })} />
        <TextField label="API key" value={form.apiKey} onChange={(apiKey) => setForm({ ...form, apiKey })} />
        <TextField label="API secret" value={form.apiSecret} onChange={(apiSecret) => setForm({ ...form, apiSecret })} type="password" />
        <TextField label="Amount" value={form.amount} onChange={(amount) => setForm({ ...form, amount })} type="number" />
        <SelectField label="Mode" value={form.paymentMode} options={["UPI", "CARD", "NETBANKING", "WALLET", "QR"]}
                     onChange={(paymentMode) => setForm({ ...form, paymentMode })} />
        <TextField label="Customer email" value={form.customerEmail} onChange={(customerEmail) => setForm({ ...form, customerEmail })} type="email" />
        <TextField label="Customer phone" value={form.customerPhone} onChange={(customerPhone) => setForm({ ...form, customerPhone })} />
        <TextField wide label="Return URL" value={form.returnUrl} onChange={(returnUrl) => setForm({ ...form, returnUrl })} />
        <button className="btn btn-dark form-command" disabled={request.busy}>Create checkout</button>
      </form>
      <Notice request={request} />
    </Page>
  );
}

function PaymentHistory() {
  const [merchantId, setMerchantId] = useMerchantId();
  const request = useRequest();
  return (
    <Page title="Payment History" action={<>
      <MerchantInput value={merchantId} onChange={setMerchantId} />
      <button className="btn btn-outline-dark" onClick={() => request.run(() => api.get(`/api/payments/merchant/${merchantId}?size=20&sort=createdAt,desc`))}>Load</button>
    </>}>
      <DataPanel title="Orders"><OrderTable orders={request.data?.content || []} /></DataPanel>
      <Notice request={request} />
    </Page>
  );
}

function RefundManager() {
  const [merchantId, setMerchantId] = useMerchantId();
  const [form, setForm] = useState({ transactionId: "", amount: "", reason: "customer requested refund" });
  const create = useRequest();
  const list = useRequest();
  async function submit(event) {
    event.preventDefault();
    const data = await create.run(() => api.post("/api/refunds", { ...form, merchantId, amount: Number(form.amount) }));
    if (data) list.run(() => api.get(`/api/refunds/merchant/${merchantId}?size=20`));
  }
  return (
    <Page title="Refund Management" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <form className="form-grid" onSubmit={submit}>
        <TextField label="Transaction ID" value={form.transactionId} onChange={(transactionId) => setForm({ ...form, transactionId })} />
        <TextField label="Amount" value={form.amount} onChange={(amount) => setForm({ ...form, amount })} type="number" />
        <TextField wide label="Reason" value={form.reason} onChange={(reason) => setForm({ ...form, reason })} />
        <button className="btn btn-dark form-command">Create refund</button>
      </form>
      <div className="toolbar"><button className="btn btn-outline-dark" disabled={!merchantId} onClick={() => list.run(() => api.get(`/api/refunds/merchant/${merchantId}?size=20`))}>Load refunds</button></div>
      <DataPanel title="Refunds"><JsonTable rows={list.data?.content || []} columns={["refundId", "transactionId", "amount", "remainingRefundableAmount", "status"]} /></DataPanel>
      <Notice request={create} /><Notice request={list} />
    </Page>
  );
}

function Settlements() {
  const [merchantId, setMerchantId] = useMerchantId();
  const now = new Date();
  const [form, setForm] = useState({
    from: new Date(now.getTime() - 86400000).toISOString(), to: now.toISOString(),
    gatewayChargePercentage: "2.00", platformFeePercentage: "1.00"
  });
  const generate = useRequest();
  const list = useRequest();
  return (
    <Page title="Settlement Reports" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); generate.run(() => api.post("/api/settlements/generate", {
        merchantId, ...form, gatewayChargePercentage: Number(form.gatewayChargePercentage), platformFeePercentage: Number(form.platformFeePercentage)
      })); }}>
        <TextField label="From ISO instant" value={form.from} onChange={(from) => setForm({ ...form, from })} />
        <TextField label="To ISO instant" value={form.to} onChange={(to) => setForm({ ...form, to })} />
        <TextField label="Gateway charge %" value={form.gatewayChargePercentage} onChange={(gatewayChargePercentage) => setForm({ ...form, gatewayChargePercentage })} />
        <TextField label="Platform fee %" value={form.platformFeePercentage} onChange={(platformFeePercentage) => setForm({ ...form, platformFeePercentage })} />
        <button className="btn btn-dark form-command">Generate batch</button>
      </form>
      {generate.data && <DataPanel title="Generated batch"><JsonTable rows={[generate.data]} columns={["settlementId", "grossAmount", "gatewayCharge", "platformFee", "gst", "netAmount", "status"]} /></DataPanel>}
      <div className="toolbar"><button className="btn btn-outline-dark" disabled={!merchantId} onClick={() => list.run(() => api.get(`/api/settlements/merchant/${merchantId}?size=20`))}>Load reports</button></div>
      <DataPanel title="Batches"><JsonTable rows={list.data?.content || []} columns={["settlementId", "from", "to", "transactionCount", "netAmount", "status"]} /></DataPanel>
      <Notice request={generate} /><Notice request={list} />
    </Page>
  );
}

function WebhookConfig() {
  const [merchantId, setMerchantId] = useMerchantId();
  const [webhookUrl, setWebhookUrl] = useState("https://merchant.example/webhooks/payments");
  const request = useRequest();
  return (
    <Page title="Webhook Configuration" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); request.run(() => api.put(`/api/merchants/${merchantId}/webhook`, { webhookUrl })); }}>
        <TextField wide label="Webhook URL" value={webhookUrl} onChange={setWebhookUrl} />
        <button className="btn btn-dark form-command">Update</button>
      </form>
      {request.data && <DataPanel title="Merchant"><JsonTable rows={[request.data]} columns={["merchantId", "businessName", "webhookUrl", "status", "kycStatus"]} /></DataPanel>}
      <Notice request={request} />
    </Page>
  );
}

function AdminDashboard() {
  return (
    <Page title="Admin Dashboard">
      <div className="metric-grid admin-grid">
        <Link to="/admin/merchants"><Metric label="Merchant management" value="KYC" /></Link>
        <Link to="/admin/gateways"><Metric label="Gateway health" value="Route" /></Link>
        <Link to="/admin/reconciliation"><Metric label="Reconciliation" value="CSV" /></Link>
        <Link to="/admin/fraud"><Metric label="Fraud alerts" value="Score" /></Link>
      </div>
    </Page>
  );
}

function MerchantManager() {
  const [register, setRegister] = useState({ businessName: "", email: "", phone: "", singlePaymentLimit: "500000.00" });
  const [status, setStatus] = useState({ merchantId: "", status: "ACTIVE", kycStatus: "VERIFIED" });
  const create = useRequest();
  const update = useRequest();
  return (
    <Page title="Merchant Management">
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); create.run(() => api.post("/api/merchants/register", { ...register, singlePaymentLimit: Number(register.singlePaymentLimit) })); }}>
        <TextField label="Business name" value={register.businessName} onChange={(businessName) => setRegister({ ...register, businessName })} />
        <TextField label="Email" value={register.email} onChange={(email) => setRegister({ ...register, email })} type="email" />
        <TextField label="Phone" value={register.phone} onChange={(phone) => setRegister({ ...register, phone })} />
        <TextField label="Single payment limit" value={register.singlePaymentLimit} onChange={(singlePaymentLimit) => setRegister({ ...register, singlePaymentLimit })} />
        <button className="btn btn-dark form-command">Register</button>
      </form>
      {create.data && <DataPanel title="Registered"><JsonTable rows={[create.data]} columns={["merchantId", "businessName", "status", "kycStatus", "singlePaymentLimit"]} /></DataPanel>}
      <form className="form-grid top-gap" onSubmit={(event) => { event.preventDefault(); update.run(() => api.put(`/api/merchants/${status.merchantId}/status`, status)); }}>
        <TextField label="Merchant ID" value={status.merchantId} onChange={(merchantId) => setStatus({ ...status, merchantId })} />
        <SelectField label="Status" value={status.status} options={["PENDING", "ACTIVE", "BLOCKED", "SUSPENDED"]} onChange={(value) => setStatus({ ...status, status: value })} />
        <SelectField label="KYC" value={status.kycStatus} options={["PENDING", "VERIFIED", "REJECTED"]} onChange={(value) => setStatus({ ...status, kycStatus: value })} />
        <button className="btn btn-outline-dark form-command">Apply status</button>
      </form>
      <Notice request={create} /><Notice request={update} />
    </Page>
  );
}

function TransactionMonitor() {
  const [transactionId, setTransactionId] = useState("");
  const request = useRequest();
  return (
    <Page title="Transaction Monitoring">
      <div className="toolbar">
        <TextField compact label="Transaction ID" value={transactionId} onChange={setTransactionId} />
        <button className="btn btn-dark" onClick={() => request.run(() => api.get(`/api/payments/status/${transactionId}`))}>Lookup</button>
      </div>
      {request.data && <DataPanel title="Transaction"><JsonTable rows={[request.data]} columns={["transactionId", "orderId", "merchantId", "amount", "status", "gateway", "failureReason"]} /></DataPanel>}
      <Notice request={request} />
    </Page>
  );
}

function GatewayHealth() {
  const request = useRequest();
  const update = useRequest();
  const [form, setForm] = useState({ gateway: "RAZORPAY_SIMULATOR", health: "ACTIVE", priority: 1, successRate: 99, timeoutMs: 750, maxRetries: 1 });
  useEffect(() => { request.run(() => api.get("/api/gateway/health")); }, []);
  return (
    <Page title="Gateway Health" action={<button className="btn btn-outline-dark" onClick={() => request.run(() => api.get("/api/gateway/health"))}>Refresh</button>}>
      <DataPanel title="Gateways"><JsonTable rows={request.data || []} columns={["gateway", "health", "successRate", "priority", "timeoutMs", "maxRetries"]} /></DataPanel>
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); update.run(() => api.put(`/api/gateway/config/${form.gateway}`, form)); }}>
        <SelectField label="Gateway" value={form.gateway} options={["RAZORPAY_SIMULATOR", "CASHFREE_SIMULATOR", "PAYU_SIMULATOR"]} onChange={(gateway) => setForm({ ...form, gateway })} />
        <SelectField label="Health" value={form.health} options={["ACTIVE", "DEGRADED", "INACTIVE"]} onChange={(health) => setForm({ ...form, health })} />
        {["priority", "successRate", "timeoutMs", "maxRetries"].map((field) => <TextField key={field} label={field} value={form[field]} type="number" onChange={(value) => setForm({ ...form, [field]: Number(value) })} />)}
        <button className="btn btn-dark form-command">Update gateway</button>
      </form>
      <Notice request={request} /><Notice request={update} />
    </Page>
  );
}

function Reconciliation() {
  const now = new Date();
  const [merchantId, setMerchantId] = useMerchantId();
  const [uploadId, setUploadId] = useState("");
  const [range, setRange] = useState({ from: new Date(now.getTime() - 86400000).toISOString(), to: now.toISOString() });
  const upload = useRequest();
  const run = useRequest();
  async function uploadFile(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const form = new FormData();
    form.append("file", file);
    const result = await upload.run(() => api.post("/api/reconciliation/upload", form));
    if (result) setUploadId(result.uploadId);
  }
  return (
    <Page title="Reconciliation Reports" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <div className="toolbar"><input className="form-control file-input" type="file" accept=".csv" onChange={uploadFile} /></div>
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); run.run(() => api.post("/api/reconciliation/run", { uploadId, merchantId, ...range })); }}>
        <TextField label="Upload ID" value={uploadId} onChange={setUploadId} />
        <TextField label="From" value={range.from} onChange={(from) => setRange({ ...range, from })} />
        <TextField label="To" value={range.to} onChange={(to) => setRange({ ...range, to })} />
        <button className="btn btn-dark form-command">Run</button>
      </form>
      <DataPanel title="Results"><JsonTable rows={run.data?.results || []} columns={["transactionId", "internalAmount", "gatewayAmount", "status", "reason"]} /></DataPanel>
      <Notice request={upload} /><Notice request={run} />
    </Page>
  );
}

function FraudConsole() {
  const [merchantId, setMerchantId] = useMerchantId();
  const [form, setForm] = useState({ transactionId: "", customerPhone: "", amount: "100.00", status: "SUCCESS", recentRefundRequests: 0, failedWebhookCallbacks: 0 });
  const assess = useRequest();
  const alerts = useRequest();
  return (
    <Page title="Fraud Alerts" action={<MerchantInput value={merchantId} onChange={setMerchantId} />}>
      <form className="form-grid" onSubmit={(event) => { event.preventDefault(); assess.run(() => api.post("/api/fraud/assess", { merchantId, ...form, amount: Number(form.amount) })); }}>
        {["transactionId", "customerPhone", "amount"].map((field) => <TextField key={field} label={field} value={form[field]} onChange={(value) => setForm({ ...form, [field]: value })} />)}
        <SelectField label="Status" value={form.status} options={["SUCCESS", "FAILED", "PENDING"]} onChange={(status) => setForm({ ...form, status })} />
        {["recentRefundRequests", "failedWebhookCallbacks"].map((field) => <TextField key={field} label={field} type="number" value={form[field]} onChange={(value) => setForm({ ...form, [field]: Number(value) })} />)}
        <button className="btn btn-dark form-command">Score</button>
      </form>
      {assess.data && <DataPanel title="Assessment"><JsonTable rows={[assess.data]} columns={["transactionId", "score", "riskLevel", "rules", "alertId"]} /></DataPanel>}
      <div className="toolbar"><button className="btn btn-outline-dark" onClick={() => alerts.run(() => api.get(`/api/fraud/alerts/${merchantId}?size=20`))}>Load alerts</button></div>
      <DataPanel title="Alert log"><JsonTable rows={alerts.data?.content || []} columns={["alertId", "transactionId", "score", "riskLevel", "reasons"]} /></DataPanel>
      <Notice request={assess} /><Notice request={alerts} />
    </Page>
  );
}

function Checkout() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const request = useRequest();
  const start = useRequest();
  const [mode, setMode] = useState("UPI");
  useEffect(() => { request.run(() => api.get(`/api/payments/orders/${orderId}`)); }, [orderId]);
  const order = request.data;
  async function pay() {
    const response = await start.run(() => api.post(`/api/payments/${orderId}/pay`, { mode }));
    if (response) window.location.assign(response.checkoutUrl || `/checkout/status/${response.transactionId}`);
  }
  return (
    <main className="checkout-stage">
      <section className="checkout-sheet">
        <Brand />
        <h1>Checkout</h1>
        {order && <>
          <KeyValue label="Order" value={order.orderId} />
          <KeyValue label="Amount" value={`${order.currency} ${order.amount}`} />
          <KeyValue label="Transaction" value={order.transactionId} />
          <div className="mode-picker">{["UPI", "CARD", "NETBANKING", "WALLET", "QR"].map((value) =>
            <button key={value} className={`btn ${mode === value ? "btn-dark" : "btn-outline-dark"}`} onClick={() => setMode(value)}>{value}</button>)}</div>
          <button className="btn btn-success w-100" onClick={pay}>Pay with {mode}</button>
          <button className="btn btn-link w-100" onClick={() => navigate(`/checkout/status/${order.transactionId}`)}>Payment status</button>
        </>}
        <Notice request={request} /><Notice request={start} />
      </section>
    </main>
  );
}

function CheckoutResult({ success = false }) {
  const { transactionId } = useParams();
  return (
    <main className="checkout-stage">
      <section className="checkout-sheet result">
        {success ? <CircleCheck className="success-mark" /> : <CircleX className="failure-mark" />}
        <h1>{success ? "Payment successful" : "Payment failed"}</h1>
        <KeyValue label="Transaction" value={transactionId} />
        <Link className="btn btn-dark" to={`/checkout/status/${transactionId}`}>View status</Link>
      </section>
    </main>
  );
}

function StatusPage() {
  const route = useParams();
  const [transactionId, setTransactionId] = useState(route.transactionId || "");
  const request = useRequest();
  useEffect(() => { if (route.transactionId) request.run(() => api.get(`/api/payments/status/${route.transactionId}`)); }, [route.transactionId]);
  return (
    <main className="checkout-stage">
      <section className="checkout-sheet wide-sheet">
        <h1>Payment status</h1>
        <div className="toolbar"><TextField compact label="Transaction ID" value={transactionId} onChange={setTransactionId} />
          <button className="btn btn-dark" onClick={() => request.run(() => api.get(`/api/payments/status/${transactionId}`))}>Refresh</button></div>
        {request.data && <JsonTable rows={[request.data]} columns={["transactionId", "orderId", "amount", "status", "gateway", "failureReason"]} />}
        <Notice request={request} />
      </section>
    </main>
  );
}

function Brand() { return <div className="brand"><span>MG</span><strong>Settlement Rail</strong></div>; }
function Page({ title, action, children }) { return <main><header className="page-head"><h1>{title}</h1><div className="toolbar">{action}</div></header>{children}</main>; }
function DataPanel({ title, children }) { return <section className="data-panel"><h2>{title}</h2>{children}</section>; }
function Metric({ label, value }) { return <article className="metric"><small>{label}</small><strong>{value}</strong></article>; }
function KeyValue({ label, value }) { return <div className="key-value"><small>{label}</small><code>{String(value || "-")}</code></div>; }
function Notice({ request }) { return request.error ? <p className="alert alert-danger">{request.error}</p> : request.busy ? <p className="alert alert-secondary">Loading</p> : null; }
function MerchantInput({ value, onChange }) { return <TextField compact label="Merchant ID" value={value} onChange={(next) => { sessionStorage.setItem("merchantId", next); onChange(next); }} />; }

function TextField({ label, value, onChange, type = "text", wide = false, compact = false }) {
  return <label className={`field ${wide ? "wide" : ""} ${compact ? "compact" : ""}`}><span>{label}</span>
    <input className="form-control" type={type} value={value ?? ""} onChange={(event) => onChange(event.target.value)} /></label>;
}
function SelectField({ label, value, options, onChange }) {
  return <label className="field"><span>{label}</span><select className="form-select" value={value} onChange={(event) => onChange(event.target.value)}>
    {options.map((option) => <option key={option}>{option}</option>)}</select></label>;
}
function JsonTable({ rows, columns }) {
  if (!rows.length) return <p className="empty">No rows</p>;
  return <div className="table-responsive"><table className="table table-sm align-middle"><thead><tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr></thead>
    <tbody>{rows.map((row, index) => <tr key={row.id || row.orderId || row.transactionId || index}>{columns.map((column) => <td key={column}>
      {column === "status" || column === "riskLevel" ? <Status value={row[column]} /> : formatCell(row[column])}</td>)}</tr>)}</tbody></table></div>;
}
function OrderTable({ orders }) { return <JsonTable rows={orders} columns={["orderId", "transactionId", "amount", "paymentMode", "status", "gateway", "checkoutUrl"]} />; }
function Status({ value }) { return <span className={`status status-${String(value || "").toLowerCase()}`}>{value || "-"}</span>; }
function formatCell(value) { return Array.isArray(value) ? value.join(", ") : value ? String(value) : "-"; }
function updateAndRemember(setter, form, merchantId) { sessionStorage.setItem("merchantId", merchantId); setter({ ...form, merchantId }); }
function useMerchantId() { return useState(defaultMerchant()); }
function useRequest() {
  const [state, setState] = useState({ busy: false, data: null, error: "" });
  return useMemo(() => ({ ...state, run: async (call) => {
    setState((current) => ({ ...current, busy: true, error: "" }));
    try {
      const response = await call();
      setState({ busy: false, data: response.data, error: "" });
      return response.data;
    } catch (error) {
      setState((current) => ({ ...current, busy: false, error: problem(error) }));
      return null;
    }
  }}), [state]);
}
