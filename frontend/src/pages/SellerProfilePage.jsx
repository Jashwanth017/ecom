import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function SellerProfilePage() {
  const { accessToken } = useAuth();
  const [formState, setFormState] = useState({
    storeName: "",
    storeDescription: ""
  });
  const [meta, setMeta] = useState({
    email: "",
    approvalStatus: "",
    rejectionReason: ""
  });
  const [feedback, setFeedback] = useState({ error: "", success: "" });

  useEffect(() => {
    async function loadProfile() {
      try {
        const data = await apiClient.getSellerProfile(accessToken);
        setFormState({
          storeName: data.storeName ?? "",
          storeDescription: data.storeDescription ?? ""
        });
        setMeta({
          email: data.email ?? "",
          approvalStatus: data.approvalStatus ?? "",
          rejectionReason: data.rejectionReason ?? ""
        });
      } catch (err) {
        setFeedback({ error: err.message || "Unable to load seller profile.", success: "" });
      }
    }

    loadProfile();
  }, [accessToken]);

  function handleChange(field) {
    return (event) => {
      setFormState((previous) => ({
        ...previous,
        [field]: event.target.value
      }));
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFeedback({ error: "", success: "" });

    try {
      const updated = await apiClient.updateSellerProfile(accessToken, formState);
      setFormState({
        storeName: updated.storeName ?? "",
        storeDescription: updated.storeDescription ?? ""
      });
      setMeta((previous) => ({
        ...previous,
        approvalStatus: updated.approvalStatus ?? previous.approvalStatus,
        rejectionReason: updated.rejectionReason ?? previous.rejectionReason
      }));
      setFeedback({ error: "", success: "Seller profile updated." });
    } catch (err) {
      setFeedback({ error: err.message || "Unable to update profile.", success: "" });
    }
  }

  return (
    <section className="seller-page seller-two-column">
      <div className="panel-card">
        <h2>Seller Profile</h2>
        <p className="muted">Update store details visible in your seller area.</p>

        <div className="profile-meta">
          <div>
            <span>Email : </span>
            <strong>{meta.email || "-"}</strong>
          </div>
          <div>
            <span>Approval Status : </span>
            <strong>{meta.approvalStatus || "-"}</strong>
          </div>
        </div>

        {meta.rejectionReason ? (
          <p className="form-feedback form-feedback-error">{meta.rejectionReason}</p>
        ) : null}
      </div>

      <form className="panel-card seller-form" onSubmit={handleSubmit}>
        <label>
          Store Name
          <input type="text" value={formState.storeName} onChange={handleChange("storeName")} />
        </label>

        <label>
          Store Description
          <textarea rows="6" value={formState.storeDescription} onChange={handleChange("storeDescription")} />
        </label>

        {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
        {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

        <button type="submit" className="button button-primary">Save Profile</button>
      </form>
    </section>
  );
}

export default SellerProfilePage;
