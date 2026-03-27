import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function AdminDashboardPage() {
  const { accessToken } = useAuth();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadSummary() {
      setLoading(true);
      try {
        const data = await apiClient.getAdminDashboardSummary(accessToken);
        setSummary(data);
        setError("");
      } catch (err) {
        setError(err.message || "Unable to load admin dashboard.");
      } finally {
        setLoading(false);
      }
    }

    loadSummary();
  }, [accessToken]);

  return (
    <section className="seller-page">
      <div className="section-heading">
        <div>
          <h2>Admin Dashboard</h2>
          <p className="muted">Platform summary and management access in one place.</p>
        </div>
      </div>

      {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}
      {loading ? <p className="empty-cell">Loading admin dashboard...</p> : null}

      <div className="stats-grid admin-stats-grid">
        <article className="stat-card">
          <span>Total Users</span>
          <strong>{summary?.totalUsers ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Total Buyers</span>
          <strong>{summary?.totalBuyers ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Total Sellers</span>
          <strong>{summary?.totalSellers ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Total Admins</span>
          <strong>{summary?.totalAdmins ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Banned Users</span>
          <strong>{summary?.bannedUsers ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Pending Approvals</span>
          <strong>{summary?.pendingSellerApprovals ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Approved Sellers</span>
          <strong>{summary?.approvedSellers ?? 0}</strong>
        </article>
        <article className="stat-card">
          <span>Rejected Sellers</span>
          <strong>{summary?.rejectedSellers ?? 0}</strong>
        </article>
      </div>

      <div className="table-card">
        <div className="section-heading">
          <div>
            <h3>Admin Controls</h3>
            <p className="muted">Open the main management areas from here.</p>
          </div>
        </div>

        <div className="admin-control-grid">
          <Link to="/admin/users" className="admin-control-card">
            <span className="admin-control-icon">👥</span>
            <h4>Users</h4>
            <p>View accounts and ban or unban users.</p>
          </Link>
          <Link to="/admin/seller-approvals" className="admin-control-card">
            <span className="admin-control-icon">✅</span>
            <h4>Seller Approvals</h4>
            <p>Review pending sellers and approve or reject them.</p>
          </Link>
          <Link to="/admin/categories" className="admin-control-card">
            <span className="admin-control-icon">🗂️</span>
            <h4>Categories</h4>
            <p>Create and manage the catalog category list.</p>
          </Link>
        </div>
      </div>
    </section>
  );
}

export default AdminDashboardPage;
