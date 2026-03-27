import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function BuyerProfilePage() {
  const { accessToken } = useAuth();
  const [formState, setFormState] = useState({
    fullName: "",
    phone: ""
  });
  const [profile, setProfile] = useState(null);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProfile();
  }, [accessToken]);

  async function loadProfile() {
    setLoading(true);

    try {
      const data = await apiClient.getBuyerProfile(accessToken);
      setProfile(data);
      setFormState({
        fullName: data.fullName ?? "",
        phone: data.phone ?? ""
      });
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (error) {
      setFeedback({ error: error.message || "Unable to load buyer profile.", success: "" });
    } finally {
      setLoading(false);
    }
  }

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
      const updatedProfile = await apiClient.updateBuyerProfile(accessToken, formState);
      setProfile(updatedProfile);
      setFormState({
        fullName: updatedProfile.fullName ?? "",
        phone: updatedProfile.phone ?? ""
      });
      setFeedback({ error: "", success: "Buyer profile updated." });
    } catch (error) {
      setFeedback({ error: error.message || "Unable to update buyer profile.", success: "" });
    }
  }

  const addressCount = profile?.addresses?.length ?? 0;

  return (
    <section className="buyer-page">
      <div className="buyer-page-hero buyer-surface-card">
        <div>
          <p className="eyebrow">Buyer Profile</p>
          <h2>Keep your buyer account up to date</h2>
          <p className="muted">
            Maintain contact details, check your saved delivery information, and jump into the next task quickly.
          </p>
        </div>
        <div className="buyer-hero-metrics">
          <div>
            <span>Saved Addresses</span>
            <strong>{addressCount}</strong>
          </div>
          <div>
            <span>Email</span>
            <strong>{profile?.email ?? "-"}</strong>
          </div>
        </div>
      </div>

      {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
      {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

      <div className="buyer-layout-grid">
        <div className="panel-card buyer-sidebar-stack">
          <div>
            <h2>Account Snapshot</h2>
            <p className="muted">Basic details from your buyer profile.</p>
          </div>

          {loading ? (
            <p className="empty-cell">Loading profile...</p>
          ) : (
            <>
              <div className="buyer-summary-row">
                <span>Buyer Profile ID</span>
                <strong>{profile?.buyerProfileId ?? "-"}</strong>
              </div>
              <div className="buyer-summary-row">
                <span>User ID</span>
                <strong>{profile?.userId ?? "-"}</strong>
              </div>
              <div className="buyer-summary-row">
                <span>Email</span>
                <strong>{profile?.email ?? "-"}</strong>
              </div>
              <div className="buyer-summary-row">
                <span>Phone</span>
                <strong>{profile?.phone || "-"}</strong>
              </div>
            </>
          )}

          <div className="buyer-shortcuts">
            <Link to="/buyer/addresses" className="buyer-shortcut-card">
              <strong>Manage Addresses</strong>
              <span>Edit delivery locations and keep them current.</span>
            </Link>
            <Link to="/buyer/orders" className="buyer-shortcut-card">
              <strong>View Orders</strong>
              <span>Open your timeline and inspect previous purchases.</span>
            </Link>
          </div>
        </div>

        <form className="panel-card seller-form" onSubmit={handleSubmit}>
          <div className="section-heading">
            <div>
              <h2>Edit Profile</h2>
              <p className="muted">These details are stored on your buyer profile.</p>
            </div>
            <button type="button" className="button button-secondary" onClick={loadProfile} disabled={loading}>
              Reload
            </button>
          </div>

          <label>
            Full Name
            <input
              type="text"
              value={formState.fullName}
              onChange={handleChange("fullName")}
              maxLength={120}
              required
            />
          </label>

          <label>
            Phone
            <input
              type="tel"
              value={formState.phone}
              onChange={handleChange("phone")}
              maxLength={20}
            />
          </label>

          <div className="buyer-address-preview">
            <div className="section-heading">
              <div>
                <h3>Saved Addresses</h3>
                <p className="muted">Preview of the addresses tied to this account.</p>
              </div>
              <Link to="/buyer/addresses" className="text-link">Open all</Link>
            </div>

            {addressCount === 0 ? (
              <p className="empty-cell">No saved addresses yet.</p>
            ) : (
              <div className="buyer-address-preview-list">
                {profile.addresses.slice(0, 2).map((address) => (
                  <div key={address.addressId} className="buyer-address-chip">
                    <strong>{address.city}, {address.state}</strong>
                    <span>{address.addressLine1}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button type="submit" className="button button-primary">Save Profile</button>
        </form>
      </div>
    </section>
  );
}

export default BuyerProfilePage;
