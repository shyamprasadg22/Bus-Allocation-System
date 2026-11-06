/**
 * Student Module - Logic for Travel Declarations & Allocation Seat Lookup
 */

document.addEventListener('DOMContentLoaded', () => {
  initStudentTabs();
  loadRoutesAndShifts();
  setupEventListeners();
  setDefaultDates();
});

let availableRoutes = [];
let availableShifts = [];

function initStudentTabs() {
  const tabBtns = document.querySelectorAll('.tabs-nav .tab-btn');
  const panels = document.querySelectorAll('.tab-panel');

  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-tab');
      tabBtns.forEach(b => b.classList.remove('active'));
      panels.forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const targetPanel = document.getElementById(targetId);
      if (targetPanel) targetPanel.classList.add('active');
    });
  });
}

function setDefaultDates() {
  const tomorrow = getTomorrowIsoDate();
  const today = getTodayIsoDate();

  const travelDateInput = document.getElementById('declTravelDate');
  if (travelDateInput) {
    travelDateInput.value = tomorrow;
    travelDateInput.min = today;
  }

  const lookupDateInput = document.getElementById('lookupDate');
  if (lookupDateInput) {
    lookupDateInput.value = tomorrow;
  }
}

async function loadRoutesAndShifts() {
  try {
    const [routesResp, shiftsResp] = await Promise.all([
      apiFetch('/student/routes'),
      apiFetch('/student/shifts')
    ]);

    availableRoutes = routesResp.data || [];
    availableShifts = shiftsResp.data || [];

    // Populate Routes dropdown
    const routeSelect = document.getElementById('declRoute');
    if (routeSelect) {
      routeSelect.innerHTML = '<option value="">-- Select Your Travel Route --</option>';
      availableRoutes.forEach(r => {
        const opt = document.createElement('option');
        opt.value = r.routeId;
        opt.textContent = `${r.routeCode} - ${r.routeName} (${r.startLocation})`;
        routeSelect.appendChild(opt);
      });
    }

    // Populate Shifts dropdown
    const shiftSelect = document.getElementById('declShift');
    if (shiftSelect) {
      shiftSelect.innerHTML = '<option value="">-- Select Operating Shift --</option>';
      availableShifts.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.shiftId;
        opt.textContent = `${s.shiftName} [Departure: ${formatTime(s.departureTime)}]`;
        shiftSelect.appendChild(opt);
      });
    }
  } catch (error) {
    showAlert('declAlert', 'danger', 'Failed to load routes and shifts. Please refresh the page.');
  }
}

function setupEventListeners() {
  // Student Verification button
  const verifyBtn = document.getElementById('btnVerifyStudent');
  if (verifyBtn) {
    verifyBtn.addEventListener('click', handleVerifyStudent);
  }

  // Declaration form submission
  const declForm = document.getElementById('declarationForm');
  if (declForm) {
    declForm.addEventListener('submit', handleDeclarationSubmit);
  }

  // Seat status lookup
  const lookupForm = document.getElementById('lookupForm');
  if (lookupForm) {
    lookupForm.addEventListener('submit', handleSeatLookup);
  }

  // History button
  const historyBtn = document.getElementById('btnViewHistory');
  if (historyBtn) {
    historyBtn.addEventListener('click', handleViewHistory);
  }
}

async function handleVerifyStudent() {
  const studentIdInput = document.getElementById('declStudentId');
  const pinInput = document.getElementById('declPin');
  const studentId = studentIdInput.value.trim().toUpperCase();
  const pin = pinInput.value.trim();

  clearAlert('declAlert');

  if (!studentId) {
    showAlert('declAlert', 'warning', 'Please enter your Student ID.');
    studentIdInput.focus();
    return;
  }
  if (!pin) {
    showAlert('declAlert', 'warning', 'Please enter your Access PIN to verify your record.');
    pinInput.focus();
    return;
  }

  try {
    const resp = await apiFetch(`/student/${encodeURIComponent(studentId)}?pin=${encodeURIComponent(pin)}`);
    const student = resp.data;

    // Show verified student info box
    const infoBox = document.getElementById('verifiedStudentInfo');
    infoBox.style.display = 'block';
    document.getElementById('infoStudentName').textContent = student.fullName;
    document.getElementById('infoStudentDept').textContent = student.department;
    document.getElementById('infoStudentRoute').textContent = `${student.assignedRouteCode} - ${student.assignedRouteName}`;

    // Auto-select assigned route if not already chosen
    const routeSelect = document.getElementById('declRoute');
    if (routeSelect && student.assignedRouteId) {
      routeSelect.value = student.assignedRouteId;
    }

    // Auto-propagate verified credentials to Lookup and History tabs for convenience
    const lookupId = document.getElementById('lookupStudentId');
    const lookupPin = document.getElementById('lookupPin');
    const histId = document.getElementById('historyStudentId');
    const histPin = document.getElementById('historyPin');
    if (lookupId) lookupId.value = studentId;
    if (lookupPin) lookupPin.value = pin;
    if (histId) histId.value = studentId;
    if (histPin) histPin.value = pin;

    showAlert('declAlert', 'success', `Student verified: ${student.fullName} (${student.department}). You may now submit or update your travel declaration.`);
  } catch (error) {
    document.getElementById('verifiedStudentInfo').style.display = 'none';
    showAlert('declAlert', 'danger', error.message || 'Student verification failed.');
  }
}

async function handleDeclarationSubmit(e) {
  e.preventDefault();
  clearAlert('declAlert');

  const studentId = document.getElementById('declStudentId').value.trim().toUpperCase();
  const pin = document.getElementById('declPin').value.trim();
  const travelDate = document.getElementById('declTravelDate').value;
  const routeId = parseInt(document.getElementById('declRoute').value, 10);
  const shiftId = parseInt(document.getElementById('declShift').value, 10);
  const willTravelRadio = document.querySelector('input[name="willTravel"]:checked');

  // Frontend validations
  if (!studentId || studentId.length < 5) {
    showAlert('declAlert', 'danger', 'Please enter a valid Student ID (alphanumeric, min 5 chars).');
    return;
  }
  if (!pin) {
    showAlert('declAlert', 'danger', 'Access PIN is required to authorize declaration submission.');
    return;
  }
  if (!travelDate) {
    showAlert('declAlert', 'danger', 'Please choose your scheduled travel date.');
    return;
  }
  if (!routeId) {
    showAlert('declAlert', 'danger', 'Please select your bus route.');
    return;
  }
  if (!shiftId) {
    showAlert('declAlert', 'danger', 'Please select your preferred shift.');
    return;
  }
  if (!willTravelRadio) {
    showAlert('declAlert', 'danger', 'Please indicate whether you will travel.');
    return;
  }

  const willTravel = willTravelRadio.value === 'true';

  const payload = {
    studentId,
    accessPin: pin,
    travelDate,
    routeId,
    shiftId,
    willTravel
  };

  const submitBtn = document.getElementById('btnSubmitDecl');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="loading-spinner"></span> Submitting...';

  try {
    const resp = await apiFetch('/student/declaration', {
      method: 'POST',
      body: payload
    });

    const d = resp.data;
    const travelStatusText = d.willTravel ? 'CONFIRMED TO TRAVEL' : 'DECLARED NOT TRAVELLING';
    
    showAlert('declAlert', 'success', 
      `Declaration Saved Successfully! Date: ${d.travelDate} | Status: ${travelStatusText} | Route: ${d.routeCode} | Shift: ${d.shiftName}. You can update this declaration at any time prior to the cutoff deadline.`);

    // Auto switch to lookup tab or update lookup fields
    document.getElementById('lookupStudentId').value = studentId;
    document.getElementById('lookupPin').value = pin;
    document.getElementById('lookupDate').value = travelDate;
    const histId = document.getElementById('historyStudentId');
    const histPin = document.getElementById('historyPin');
    if (histId) histId.value = studentId;
    if (histPin) histPin.value = pin;

  } catch (error) {
    showAlert('declAlert', 'danger', error.message || 'Failed to submit declaration.');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = 'Save Declaration';
  }
}

async function handleSeatLookup(e) {
  e.preventDefault();
  clearAlert('lookupAlert');

  const studentId = document.getElementById('lookupStudentId').value.trim().toUpperCase();
  const pin = document.getElementById('lookupPin').value.trim();
  const travelDate = document.getElementById('lookupDate').value;

  if (!studentId || !pin) {
    showAlert('lookupAlert', 'warning', 'Please enter your Student ID and Access PIN.');
    return;
  }

  const lookupBtn = document.getElementById('btnLookup');
  lookupBtn.disabled = true;
  lookupBtn.innerHTML = '<span class="loading-spinner"></span> Checking...';

  try {
    const resp = await apiFetch(`/student/${encodeURIComponent(studentId)}/status?date=${encodeURIComponent(travelDate)}&pin=${encodeURIComponent(pin)}`);
    const data = resp.data;
    renderSeatLookupResult(data);
  } catch (error) {
    document.getElementById('lookupResultCard').style.display = 'none';
    showAlert('lookupAlert', 'danger', error.message || 'Lookup failed.');
  } finally {
    lookupBtn.disabled = false;
    lookupBtn.innerHTML = 'Check Allocation Status';
  }
}

function renderSeatLookupResult(data) {
  const resultCard = document.getElementById('lookupResultCard');
  resultCard.style.display = 'block';

  const statusDesc = document.getElementById('resultStatusDesc');
  const declInfo = document.getElementById('resultDeclInfo');
  const seatInfo = document.getElementById('resultSeatInfo');

  statusDesc.textContent = data.statusDescription;

  const decl = data.declaration;
  const assign = data.assignment;

  if (!decl) {
    declInfo.innerHTML = `
      <div class="empty-state">
        <p>No travel declaration found for date <strong>${formatDate(data.queryDate)}</strong>.</p>
        <p style="margin-top: 6px;">Submit a declaration in Tab 1 if you plan to travel.</p>
      </div>
    `;
    seatInfo.innerHTML = '';
    return;
  }

  declInfo.innerHTML = `
    <table class="data-table">
      <tr><th style="width: 35%;">Travel Date</th><td>${formatDate(decl.travelDate)}</td></tr>
      <tr><th>Declaration Status</th><td><span class="badge ${decl.willTravel ? 'badge-success' : 'badge-secondary'}">${decl.willTravel ? 'Confirmed Travelling' : 'Not Travelling'}</span></td></tr>
      <tr><th>Selected Route</th><td>${decl.routeCode} - ${decl.routeName}</td></tr>
      <tr><th>Selected Shift</th><td>${decl.shiftName}</td></tr>
      <tr><th>Submitted At</th><td>${decl.declaredAt ? new Date(decl.declaredAt).toLocaleString() : '-'}</td></tr>
    </table>
  `;

  if (assign) {
    seatInfo.innerHTML = `
      <div style="background-color: var(--status-success-bg); border: 1px solid var(--status-success-border); padding: 16px; border-radius: var(--radius-sm); margin-top: 14px;">
        <h4 style="color: var(--status-success-text); margin-bottom: 8px;">Seat Assignment Confirmed</h4>
        <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; font-size: 14px;">
          <div><strong>Assigned Bus:</strong> ${assign.busLabel} (${assign.busNumber})</div>
          <div><strong>Seat Number:</strong> <span style="font-size: 18px; font-weight: 700; color: var(--primary);">Seat #${assign.seatNumber}</span></div>
          <div><strong>Route:</strong> ${assign.routeCode} - ${assign.routeName}</div>
          <div><strong>Shift:</strong> ${assign.shiftName}</div>
        </div>
      </div>
    `;
  } else if (decl.willTravel) {
    seatInfo.innerHTML = `
      <div class="alert alert-info" style="margin-top: 14px;">
        <strong>Bus Allocation Pending:</strong> Your travel declaration is recorded. The administrative allocation process has not been finalized yet for this date. Please check back after the evening allocation deadline.
      </div>
    `;
  } else {
    seatInfo.innerHTML = '';
  }
}

async function handleViewHistory() {
  const studentId = document.getElementById('historyStudentId').value.trim().toUpperCase();
  const pin = document.getElementById('historyPin').value.trim();

  clearAlert('historyAlert');

  if (!studentId || !pin) {
    showAlert('historyAlert', 'warning', 'Please provide Student ID and Access PIN to load history.');
    return;
  }

  try {
    const resp = await apiFetch(`/student/${encodeURIComponent(studentId)}/history?pin=${encodeURIComponent(pin)}`);
    const history = resp.data || [];
    renderHistoryTable(history);
  } catch (error) {
    showAlert('historyAlert', 'danger', error.message || 'Failed to load history.');
  }
}

function renderHistoryTable(list) {
  const tbody = document.getElementById('historyTableBody');
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No past declarations found.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(d => `
    <tr>
      <td>${formatDate(d.travelDate)}</td>
      <td>${d.routeCode} - ${d.routeName}</td>
      <td>${d.shiftName}</td>
      <td>
        <span class="badge ${d.willTravel ? 'badge-success' : 'badge-secondary'}">
          ${d.willTravel ? 'Will Travel' : 'Not Travelling'}
        </span>
      </td>
      <td>${d.updatedAt ? new Date(d.updatedAt).toLocaleString() : new Date(d.declaredAt).toLocaleString()}</td>
    </tr>
  `).join('');
}
