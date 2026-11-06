/**
 * College Bus Seat Allocation System - Common Client Utilities
 */

const API_BASE = '/api';

/**
 * Perform an API call with automatic JSON serialization, headers, and error handling.
 */
async function apiFetch(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...(options.headers || {})
  };

  // Attach admin session token if present in localStorage
  const adminToken = localStorage.getItem('adminToken');
  if (adminToken) {
    headers['X-Admin-Token'] = adminToken;
  }

  const fetchOptions = {
    ...options,
    headers
  };

  if (fetchOptions.body && typeof fetchOptions.body === 'object') {
    fetchOptions.body = JSON.stringify(fetchOptions.body);
  }

  try {
    const response = await fetch(url, fetchOptions);
    const contentType = response.headers.get('content-type');
    let data = null;

    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = { success: response.ok, message: text };
    }

    if (!response.ok) {
      const errorMsg = data && data.message ? data.message : `Server error (${response.status})`;
      throw new Error(errorMsg);
    }

    return data;
  } catch (error) {
    console.error(`API Error on [${endpoint}]:`, error);
    throw error;
  }
}

/**
 * Format ISO date string into readable standard college date: e.g. "Tuesday, 14 Oct 2026"
 */
function formatDate(isoDate) {
  if (!isoDate) return '-';
  const parts = isoDate.split('-');
  if (parts.length !== 3) return isoDate;
  const d = new Date(parts[0], parts[1] - 1, parts[2]);
  return d.toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

/**
 * Format 24-hr time string "07:30:00" to "07:30 AM"
 */
function formatTime(timeStr) {
  if (!timeStr) return '-';
  const [h, m] = timeStr.split(':');
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour % 12 || 12;
  return `${String(h12).padStart(2, '0')}:${m} ${ampm}`;
}

/**
 * Display alert banner inside a container element
 */
function showAlert(containerId, type, message) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.innerHTML = `
    <div class="alert alert-${type}" role="alert">
      ${escapeHtml(message)}
    </div>
  `;
}

/**
 * Clear alert container
 */
function clearAlert(containerId) {
  const container = document.getElementById(containerId);
  if (container) container.innerHTML = '';
}

/**
 * HTML escaper to prevent XSS in dynamic rendering
 */
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Get tomorrow's ISO date string (YYYY-MM-DD)
 */
function getTomorrowIsoDate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().split('T')[0];
}

/**
 * Get today's ISO date string (YYYY-MM-DD)
 */
function getTodayIsoDate() {
  return new Date().toISOString().split('T')[0];
}
