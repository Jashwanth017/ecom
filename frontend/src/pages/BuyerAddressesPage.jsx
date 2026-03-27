import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

const initialFormState = {
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: ""
};

function BuyerAddressesPage() {
  const { accessToken } = useAuth();
  const [addresses, setAddresses] = useState([]);
  const [formState, setFormState] = useState(initialFormState);
  const [editingAddressId, setEditingAddressId] = useState(null);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loading, setLoading] = useState(true);
  const [activeAddressId, setActiveAddressId] = useState(null);

  useEffect(() => {
    loadAddresses();
  }, [accessToken]);

  async function loadAddresses() {
    setLoading(true);

    try {
      const data = await apiClient.getBuyerAddresses(accessToken);
      setAddresses(data ?? []);
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (error) {
      setFeedback({ error: error.message || "Unable to load addresses.", success: "" });
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

  function handleStartEdit(address) {
    setEditingAddressId(address.addressId);
    setFormState({
      addressLine1: address.addressLine1 ?? "",
      addressLine2: address.addressLine2 ?? "",
      city: address.city ?? "",
      state: address.state ?? "",
      postalCode: address.postalCode ?? ""
    });
    setFeedback({ error: "", success: "" });
  }

  function handleCancelEdit() {
    setEditingAddressId(null);
    setFormState(initialFormState);
    setFeedback({ error: "", success: "" });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFeedback({ error: "", success: "" });

    try {
      if (editingAddressId) {
        const updatedAddress = await apiClient.updateBuyerAddress(accessToken, editingAddressId, formState);
        setAddresses((previous) =>
          previous.map((address) => (address.addressId === editingAddressId ? updatedAddress : address))
        );
        setFeedback({ error: "", success: "Address updated." });
      } else {
        const createdAddress = await apiClient.createBuyerAddress(accessToken, formState);
        setAddresses((previous) => [createdAddress, ...previous]);
        setFeedback({ error: "", success: "Address added." });
      }

      setEditingAddressId(null);
      setFormState(initialFormState);
    } catch (error) {
      setFeedback({ error: error.message || "Unable to save address.", success: "" });
    }
  }

  async function handleDelete(addressId) {
    setActiveAddressId(addressId);
    setFeedback({ error: "", success: "" });

    try {
      await apiClient.deleteBuyerAddress(accessToken, addressId);
      setAddresses((previous) => previous.filter((address) => address.addressId !== addressId));
      if (editingAddressId === addressId) {
        handleCancelEdit();
      }
      setFeedback({ error: "", success: "Address removed." });
    } catch (error) {
      setFeedback({ error: error.message || "Unable to delete address.", success: "" });
    } finally {
      setActiveAddressId(null);
    }
  }

  return (
    <section className="buyer-page">
      <div className="buyer-page-hero buyer-gradient-card">
        <div>
          <p className="eyebrow">Addresses</p>
          <h2>Organize delivery locations without friction</h2>
          <p className="muted">
            Add new addresses, edit old ones, and keep your shipping details easy to reuse.
          </p>
        </div>
        <div className="buyer-hero-metrics">
          <div>
            <span>Total Saved</span>
            <strong>{addresses.length}</strong>
          </div>
          <div>
            <span>Mode</span>
            <strong>{editingAddressId ? "Editing" : "Adding"}</strong>
          </div>
        </div>
      </div>

      {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
      {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

      <div className="buyer-layout-grid">
        <div className="table-card">
          <div className="section-heading">
            <div>
              <h2>Saved Addresses</h2>
              <p className="muted">Update any saved address or remove entries you no longer use.</p>
            </div>
            <button type="button" className="button button-secondary" onClick={loadAddresses} disabled={loading}>
              Refresh
            </button>
          </div>

          {loading ? (
            <p className="empty-cell">Loading addresses...</p>
          ) : addresses.length === 0 ? (
            <p className="empty-cell">No addresses saved yet. Add your first one from the form.</p>
          ) : (
            <div className="buyer-list">
              {addresses.map((address) => (
                <article key={address.addressId} className="buyer-address-card">
                  <div className="buyer-item-head">
                    <div>
                      <h3>{address.city}, {address.state}</h3>
                      <p className="muted">Postal code: {address.postalCode}</p>
                    </div>
                    <strong>#{address.addressId}</strong>
                  </div>

                  <div className="buyer-address-lines">
                    <span>{address.addressLine1}</span>
                    {address.addressLine2 ? <span>{address.addressLine2}</span> : null}
                  </div>

                  <div className="buyer-action-row">
                    <button type="button" className="button button-secondary" onClick={() => handleStartEdit(address)}>
                      Edit
                    </button>
                    <button
                      type="button"
                      className="button button-secondary"
                      onClick={() => handleDelete(address.addressId)}
                      disabled={activeAddressId === address.addressId}
                    >
                      Delete
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>

        <form className="panel-card seller-form" onSubmit={handleSubmit}>
          <div className="section-heading">
            <div>
              <h2>{editingAddressId ? "Edit Address" : "Add Address"}</h2>
              <p className="muted">Fields match the backend validation rules for buyer addresses.</p>
            </div>
            {editingAddressId ? (
              <button type="button" className="button button-secondary" onClick={handleCancelEdit}>
                Cancel
              </button>
            ) : null}
          </div>

          <label>
            Address Line 1
            <input
              type="text"
              value={formState.addressLine1}
              onChange={handleChange("addressLine1")}
              maxLength={255}
              required
            />
          </label>

          <label>
            Address Line 2
            <input
              type="text"
              value={formState.addressLine2}
              onChange={handleChange("addressLine2")}
              maxLength={255}
            />
          </label>

          <div className="inline-field-grid">
            <label>
              City
              <input
                type="text"
                value={formState.city}
                onChange={handleChange("city")}
                maxLength={100}
                required
              />
            </label>
            <label>
              State
              <input
                type="text"
                value={formState.state}
                onChange={handleChange("state")}
                maxLength={100}
                required
              />
            </label>
          </div>

          <label>
            Postal Code
            <input
              type="text"
              value={formState.postalCode}
              onChange={handleChange("postalCode")}
              maxLength={20}
              required
            />
          </label>

          <button type="submit" className="button button-primary">
            {editingAddressId ? "Save Changes" : "Add Address"}
          </button>
        </form>
      </div>
    </section>
  );
}

export default BuyerAddressesPage;
