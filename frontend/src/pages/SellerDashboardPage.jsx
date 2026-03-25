import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function SellerDashboardPage() {
  const { accessToken } = useAuth();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadSummary() {
      try {
        const data = await apiClient.getSellerDashboardSummary(accessToken);
        setSummary(data);
      } catch (err) {
        setError(err.message || "Unable to load dashboard summary.");
      }
    }

    loadSummary();
  }, [accessToken]);

  return (
    <section className="seller-page">
      <div className="section-heading">
        <div>
          <h2>Seller Summary</h2>
          <p className="muted">Overview of orders, products, and revenue.</p>
        </div>
      </div>

      {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}

      <div className="stats-grid">
        <article className="stat-card">
          <span>Total Orders</span>
          <strong>{summary?.totalOrders ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Total Products</span>
          <strong>{summary?.totalProducts ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Total Money Generated</span>
          <strong>{formatCurrency(summary?.totalMoneyGenerated ?? 0)}</strong>
        </article>
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value));
}

export default SellerDashboardPage;
