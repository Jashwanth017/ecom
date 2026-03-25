import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function AdminSellerApprovalsPage() {
  const { accessToken } = useAuth();
  const [sellers, setSellers] = useState([]);
  const [reasons, setReasons] = useState({});
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loadingId, setLoadingId] = useState(null);

  useEffect(() => {
    loadPendingSellers();
  }, [accessToken]);

  async function loadPendingSellers() {
    try {
      const data = await apiClient.getPendingSellerApprovals(accessToken);
      setSellers(data ?? []);
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (err) {
      setFeedback({ error: err.message || "Unable to load pending sellers.", success: "" });
    }
  }

  function handleReasonChange(userId) {
    return (event) => {
      setReasons((previous) => ({
        ...previous,
        [userId]: event.target.value
      }));
    };
  }

  async function handleApproval(seller, action) {
    const targetId = seller.sellerProfileId;
    setLoadingId(seller.userId);
    setFeedback({ error: "", success: "" });

    if (!targetId) {
      setFeedback({
        error: "sellerProfileId is missing in the pending seller response. Restart the backend so the latest AdminUserResponse is used.",
        success: ""
      });
      setLoadingId(null);
      return;
    }

    try {
      if (action === "approve") {
        await apiClient.approveSeller(accessToken, targetId);
      } else {
        await apiClient.rejectSeller(accessToken, targetId, reasons[seller.userId] ?? "");
      }

      setSellers((previous) => previous.filter((item) => item.userId !== seller.userId));
      setFeedback({
        error: "",
        success: action === "approve" ? "Seller approved successfully." : "Seller rejected successfully."
      });
    } catch (err) {
      setFeedback({ error: err.message || "Unable to update seller approval.", success: "" });
    } finally {
      setLoadingId(null);
    }
  }

  return (
    <section className="seller-page">
      <div className="section-heading">
        <div>
          <h2>Seller Approvals</h2>
          <p className="muted">Review pending sellers and approve or reject them.</p>
        </div>
      </div>

      {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
      {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

      <div className="product-management-grid">
        {sellers.length === 0 ? (
          <div className="table-card">
            <p className="empty-cell">No pending seller approvals right now.</p>
          </div>
        ) : (
          sellers.map((seller) => (
            <article key={seller.userId} className="seller-product-card">
              <div className="seller-product-head">
                <div>
                  <span className="product-category">{seller.role}</span>
                  <h3>{seller.storeName || seller.email}</h3>
                </div>
                <strong>{seller.sellerApprovalStatus ?? "PENDING"}</strong>
              </div>

                <div className="seller-product-meta">
                  <span>Email: {seller.email}</span>
                  <span>User ID: {seller.userId}</span>
                  <span>Seller Profile ID: {seller.sellerProfileId ?? "-"}</span>
                  <span>Created: {formatDate(seller.createdAt)}</span>
                </div>

              <label className="seller-form">
                Rejection Reason
                <textarea
                  rows="4"
                  value={reasons[seller.userId] ?? ""}
                  onChange={handleReasonChange(seller.userId)}
                  placeholder="Optional reason for rejection"
                />
              </label>

              <div className="seller-product-actions">
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() => handleApproval(seller, "approve")}
                  disabled={loadingId === seller.userId || !seller.sellerProfileId}
                >
                  Approve
                </button>
                <button
                  type="button"
                  className="button button-secondary"
                  onClick={() => handleApproval(seller, "reject")}
                  disabled={loadingId === seller.userId || !seller.sellerProfileId}
                >
                  Reject
                </button>
              </div>
            </article>
          ))
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

export default AdminSellerApprovalsPage;
