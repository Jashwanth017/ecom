import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function AdminUsersPage() {
  const { accessToken } = useAuth();
  const [filters, setFilters] = useState({ role: "", status: "" });
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loadingUserId, setLoadingUserId] = useState(null);
  const [loadingList, setLoadingList] = useState(true);

  useEffect(() => {
    loadUsers();
  }, [accessToken, filters.role, filters.status]);

  async function loadUsers() {
    setLoadingList(true);
    try {
      const data = await apiClient.getAdminUsers(accessToken, filters);
      setUsers(data ?? []);
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (err) {
      setFeedback({ error: err.message || "Unable to load users.", success: "" });
    } finally {
      setLoadingList(false);
    }
  }

  function handleFilterChange(field) {
    return (event) => {
      setFilters((previous) => ({
        ...previous,
        [field]: event.target.value
      }));
      setSelectedUser(null);
    };
  }

  async function handleViewUser(userId) {
    setLoadingUserId(userId);
    setFeedback({ error: "", success: "" });

    try {
      const data = await apiClient.getAdminUser(accessToken, userId);
      setSelectedUser(data);
    } catch (err) {
      setFeedback({ error: err.message || "Unable to load user details.", success: "" });
    } finally {
      setLoadingUserId(null);
    }
  }

  async function handleStatusAction(userId, action) {
    setLoadingUserId(userId);
    setFeedback({ error: "", success: "" });

    try {
      const updated = action === "ban"
        ? await apiClient.banAdminUser(accessToken, userId)
        : await apiClient.unbanAdminUser(accessToken, userId);

      setUsers((previous) => previous.map((user) => (user.userId === userId ? updated : user)));
      setSelectedUser((previous) => (previous?.userId === userId ? updated : previous));
      setFeedback({
        error: "",
        success: action === "ban" ? "User banned successfully." : "User unbanned successfully."
      });
    } catch (err) {
      setFeedback({ error: err.message || "Unable to update user status.", success: "" });
    } finally {
      setLoadingUserId(null);
    }
  }

  return (
    <section className="seller-page admin-users-layout">
      <div className="table-card">
        <div className="section-heading">
          <div>
            <h2>Users</h2>
            <p className="muted">View all users, filter them, and control account status.</p>
          </div>
        </div>

        <div className="inline-field-grid admin-filter-grid">
          <label>
            Role
            <select value={filters.role} onChange={handleFilterChange("role")}>
              <option value="">All</option>
              <option value="BUYER">Buyer</option>
              <option value="SELLER">Seller</option>
              <option value="ADMIN">Admin</option>
            </select>
          </label>
          <label>
            Status
            <select value={filters.status} onChange={handleFilterChange("status")}>
              <option value="">All</option>
              <option value="ACTIVE">Active</option>
              <option value="BANNED">Banned</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </label>
        </div>

        {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
        {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

        <div className="admin-user-list">
          {loadingList ? (
            <p className="empty-cell">Loading users...</p>
          ) : users.length === 0 ? (
            <p className="empty-cell">No users found for the selected filters.</p>
          ) : (
            users.map((user) => (
              <article
                key={user.userId}
                className={`admin-user-card ${selectedUser?.userId === user.userId ? "active" : ""}`}
              >
                <div className="admin-user-card-head">
                  <div>
                    <h3>{user.email}</h3>
                    <p>ID #{user.userId}</p>
                  </div>
                  <span className="admin-role-badge">{user.role}</span>
                </div>

                <div className="admin-user-meta">
                  <span>Status: {user.status}</span>
                  <span>Approval: {user.sellerApprovalStatus ?? "-"}</span>
                </div>

                <div className="admin-action-row">
                  <button
                    type="button"
                    className="button button-secondary"
                    onClick={() => handleViewUser(user.userId)}
                    disabled={loadingUserId === user.userId}
                  >
                    View Details
                  </button>
                  {user.status === "BANNED" ? (
                    <button
                      type="button"
                      className="button button-secondary"
                      onClick={() => handleStatusAction(user.userId, "unban")}
                      disabled={loadingUserId === user.userId}
                    >
                      Unban
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="button button-secondary"
                      onClick={() => handleStatusAction(user.userId, "ban")}
                      disabled={loadingUserId === user.userId}
                    >
                      Ban
                    </button>
                  )}
                </div>
              </article>
            ))
          )}
        </div>
      </div>

      <div className="panel-card admin-detail-panel">
        <h2>User Detail</h2>
        <p className="muted">Select a user from the list to inspect full details.</p>

        {selectedUser ? (
          <div className="admin-detail-grid">
            <div><span>User ID</span><strong>{selectedUser.userId}</strong></div>
            <div><span>Seller Profile ID</span><strong>{selectedUser.sellerProfileId ?? "-"}</strong></div>
            <div><span>Email</span><strong>{selectedUser.email}</strong></div>
            <div><span>Role</span><strong>{selectedUser.role}</strong></div>
            <div><span>Status</span><strong>{selectedUser.status}</strong></div>
            <div><span>Seller Approval</span><strong>{selectedUser.sellerApprovalStatus ?? "-"}</strong></div>
            <div><span>Store Name</span><strong>{selectedUser.storeName ?? "-"}</strong></div>
            <div><span>Created At</span><strong>{formatDate(selectedUser.createdAt)}</strong></div>
          </div>
        ) : (
          <p className="empty-cell">No user selected yet.</p>
        )}
      </div>
    </section>
  );
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

export default AdminUsersPage;
