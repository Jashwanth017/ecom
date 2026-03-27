import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function BuyerCartPage() {
  const { accessToken } = useAuth();
  const [cart, setCart] = useState({ cartId: null, buyerId: null, items: [], totalAmount: 0 });
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState("");
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loading, setLoading] = useState(true);
  const [activeItemId, setActiveItemId] = useState(null);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [lastOrder, setLastOrder] = useState(null);

  useEffect(() => {
    loadCheckoutData();
  }, [accessToken]);

  async function loadCheckoutData() {
    setLoading(true);
    try {
      const [cartData, addressData] = await Promise.all([
        apiClient.getBuyerCart(accessToken),
        apiClient.getBuyerAddresses(accessToken)
      ]);
      const nextAddresses = addressData ?? [];
      setCart(cartData ?? { cartId: null, buyerId: null, items: [], totalAmount: 0 });
      setAddresses(nextAddresses);
      setSelectedAddressId((previous) => {
        if (previous && nextAddresses.some((address) => String(address.addressId) === String(previous))) {
          return previous;
        }
        return nextAddresses[0] ? String(nextAddresses[0].addressId) : "";
      });
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (error) {
      setFeedback({ error: error.message || "Unable to load checkout data.", success: "" });
    } finally {
      setLoading(false);
    }
  }

  async function handleQuantityChange(item, nextQuantity) {
    if (nextQuantity < 1) {
      return;
    }

    setActiveItemId(item.cartItemId);
    setFeedback({ error: "", success: "" });

    try {
      const updatedCart = await apiClient.updateBuyerCartItem(accessToken, item.cartItemId, nextQuantity);
      setCart(updatedCart);
    } catch (error) {
      setFeedback({ error: error.message || "Unable to update item quantity.", success: "" });
    } finally {
      setActiveItemId(null);
    }
  }

  async function handleRemoveItem(cartItemId) {
    setActiveItemId(cartItemId);
    setFeedback({ error: "", success: "" });

    try {
      const updatedCart = await apiClient.removeBuyerCartItem(accessToken, cartItemId);
      setCart(updatedCart);
      setFeedback({ error: "", success: "Item removed from cart." });
    } catch (error) {
      setFeedback({ error: error.message || "Unable to remove item from cart.", success: "" });
    } finally {
      setActiveItemId(null);
    }
  }

  async function handlePlaceOrder() {
    if (!selectedAddressId) {
      setFeedback({ error: "Select a delivery address before placing the order.", success: "" });
      return;
    }

    setIsPlacingOrder(true);
    setFeedback({ error: "", success: "" });

    try {
      const createdOrder = await apiClient.placeBuyerOrder(accessToken, Number(selectedAddressId));
      setLastOrder(createdOrder);
      setCart((previous) => ({
        ...previous,
        items: [],
        totalAmount: 0
      }));
      setFeedback({ error: "", success: `Order #${createdOrder.orderId} placed successfully.` });
    } catch (error) {
      setFeedback({ error: error.message || "Unable to place your order.", success: "" });
    } finally {
      setIsPlacingOrder(false);
    }
  }

  const itemCount = cart.items.reduce((total, item) => total + Number(item.quantity ?? 0), 0);

  return (
    <section className="buyer-page">
      <div className="buyer-page-hero buyer-gradient-card">
        <div>
          <p className="eyebrow">Buyer Cart</p>
          <h2>Review everything before checkout</h2>
          <p className="muted">
            Adjust quantities, remove products, and place your next order in one flow.
          </p>
        </div>

        <div className="buyer-hero-metrics">
          <div>
            <span>Items</span>
            <strong>{itemCount}</strong>
          </div>
          <div>
            <span>Subtotal</span>
            <strong>{formatCurrency(cart.totalAmount)}</strong>
          </div>
        </div>
      </div>

      {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
      {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

      <div className="buyer-layout-grid">
        <div className="table-card">
          <div className="section-heading">
            <div>
              <h2>Your Items</h2>
              <p className="muted">Only active cart items are shown here.</p>
            </div>
            <button type="button" className="button button-secondary" onClick={loadCheckoutData} disabled={loading}>
              Refresh
            </button>
          </div>

          {loading ? (
            <p className="empty-cell">Loading cart...</p>
          ) : cart.items.length === 0 ? (
            <div className="buyer-empty-card">
              <h3>Your cart is empty</h3>
              <p className="muted">Add products from the catalog to start building an order.</p>
              <Link to="/products" className="button button-primary">Browse Products</Link>
            </div>
          ) : (
            <div className="buyer-list">
              {cart.items.map((item) => (
                <article key={item.cartItemId} className="buyer-list-card">
                  <div className="buyer-item-visual">
                    {item.imageUrl ? (
                      <img src={item.imageUrl} alt={item.productName} className="buyer-item-image" />
                    ) : (
                      <div className="buyer-item-image buyer-item-image-fallback">{item.productName?.charAt(0) ?? "P"}</div>
                    )}
                  </div>

                  <div className="buyer-item-copy">
                    <div className="buyer-item-head">
                      <div>
                        <h3>{item.productName}</h3>
                        <p className="muted">Product #{item.productId}</p>
                      </div>
                      <strong>{formatCurrency(item.lineTotal)}</strong>
                    </div>

                    <div className="buyer-item-meta">
                      <span>Unit price: {formatCurrency(item.price)}</span>
                      <span>Quantity: {item.quantity}</span>
                    </div>

                    <div className="buyer-action-row">
                      <button
                        type="button"
                        className="button button-secondary"
                        onClick={() => handleQuantityChange(item, item.quantity - 1)}
                        disabled={activeItemId === item.cartItemId || item.quantity <= 1}
                      >
                        -1
                      </button>
                      <button
                        type="button"
                        className="button button-secondary"
                        onClick={() => handleQuantityChange(item, item.quantity + 1)}
                        disabled={activeItemId === item.cartItemId}
                      >
                        +1
                      </button>
                      <button
                        type="button"
                        className="button button-secondary"
                        onClick={() => handleRemoveItem(item.cartItemId)}
                        disabled={activeItemId === item.cartItemId}
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>

        <aside className="panel-card buyer-sidebar-stack">
          <div className="buyer-summary-card">
            <h3>Checkout Summary</h3>
            <div className="buyer-summary-row">
              <span>Total items</span>
              <strong>{itemCount}</strong>
            </div>
            <div className="buyer-summary-row">
              <span>Order total</span>
              <strong>{formatCurrency(cart.totalAmount)}</strong>
            </div>
            <div className="buyer-address-selector">
              <label>
                Delivery Address
                <select
                  value={selectedAddressId}
                  onChange={(event) => setSelectedAddressId(event.target.value)}
                  disabled={loading || addresses.length === 0}
                >
                  {addresses.length === 0 ? (
                    <option value="">No saved addresses</option>
                  ) : (
                    addresses.map((address) => (
                      <option key={address.addressId} value={address.addressId}>
                        {formatAddressOption(address)}
                      </option>
                    ))
                  )}
                </select>
              </label>
              {addresses.length === 0 ? (
                <p className="muted">Add at least one address before you checkout.</p>
              ) : null}
            </div>
            <button
              type="button"
              className="button button-primary buyer-wide-button"
              onClick={handlePlaceOrder}
              disabled={cart.items.length === 0 || isPlacingOrder || addresses.length === 0}
            >
              {isPlacingOrder ? "Placing Order..." : "Place Order"}
            </button>
            <p className="muted">Orders are created directly from the items currently in your cart.</p>
          </div>

          <div className="buyer-shortcuts">
            <Link to="/buyer/orders" className="buyer-shortcut-card">
              <strong>Orders</strong>
              <span>Track placed orders and inspect line items.</span>
            </Link>
            <Link to="/buyer/addresses" className="buyer-shortcut-card">
              <strong>Addresses</strong>
              <span>Keep delivery details organized in one place.</span>
            </Link>
          </div>

          {lastOrder ? (
            <div className="buyer-receipt-card">
              <p className="eyebrow">Latest Order</p>
              <h3>Order #{lastOrder.orderId}</h3>
              <div className="buyer-summary-row">
                <span>Status</span>
                <strong>{lastOrder.status}</strong>
              </div>
              <div className="buyer-summary-row">
                <span>Placed</span>
                <strong>{formatDate(lastOrder.placedAt)}</strong>
              </div>
              <div className="buyer-summary-row">
                <span>Total</span>
                <strong>{formatCurrency(lastOrder.totalAmount)}</strong>
              </div>
              {lastOrder.deliveryAddress ? (
                <div className="buyer-delivery-card">
                  <span>Delivering to</span>
                  <strong>{lastOrder.deliveryAddress.addressLine1}</strong>
                  {lastOrder.deliveryAddress.addressLine2 ? <span>{lastOrder.deliveryAddress.addressLine2}</span> : null}
                  <span>
                    {lastOrder.deliveryAddress.city}, {lastOrder.deliveryAddress.state} {lastOrder.deliveryAddress.postalCode}
                  </span>
                </div>
              ) : null}
            </div>
          ) : null}
        </aside>
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value ?? 0));
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

function formatAddressOption(address) {
  return `${address.addressLine1}, ${address.city}, ${address.state} ${address.postalCode}`;
}

export default BuyerCartPage;
