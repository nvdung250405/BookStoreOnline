/**
 * suppliers.js - Supplier & Partner Management
 */
const suppliers = {
    supplierData: [],

    loadList: async () => {
        try {
            const res = await api.get('/admin/suppliers');
            
            // Xử lý lấy đúng mảng dữ liệu
            const list = res.data.data ? res.data.data : (res.data || []);
            suppliers.supplierData = list;
            
            const tbody = $("#suppliers-list");
            if (!tbody.length) return;
            tbody.empty();

            if (suppliers.supplierData.length === 0) {
                tbody.append('<tr><td colspan="3" class="text-center py-4 text-muted">No partners found.</td></tr>');
                return;
            }

            suppliers.supplierData.forEach(s => {
                const id = s.supplierId;
                const name = s.supplierName;
                const contactInfo = s.contactInfo || '---'; 

                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold">${name}</td>
                        <td>${contactInfo}</td>
                        <td class="text-end pe-4">
                            <button onclick="suppliers.edit(${id})" class="btn btn-sm btn-light rounded-3 me-1">Edit</button>
                            <button onclick="suppliers.delete(${id})" class="btn btn-sm btn-outline-danger rounded-3">Delete</button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) { 
            console.error(e); 
            api.showToast("Failed to load suppliers", "error");
        }
    },

    addSupplier: () => {
        $("#supplierId").val('');
        $("#supplierName").val('');
        $("#supplierContactInfo").val('');
        
        $("#supplierModalTitle").text("Add New Partner");
        const modal = new bootstrap.Modal(document.getElementById('supplierModal'));
        modal.show();
    },

    edit: (id) => {
        const s = suppliers.supplierData.find(item => item.supplierId === id);
        if (!s) return;

        $("#supplierId").val(s.supplierId);
        $("#supplierName").val(s.supplierName);
        $("#supplierContactInfo").val(s.contactInfo || '');

        $("#supplierModalTitle").text("Edit Partner");
        const modal = new bootstrap.Modal(document.getElementById('supplierModal'));
        modal.show();
    },

    saveSupplier: async () => {
        const id = $("#supplierId").val();
        
        // Chỉ gửi đúng 2 trường mà Backend đang cần
        const data = {
            supplierName: $("#supplierName").val().trim(),
            contactInfo: $("#supplierContactInfo").val().trim() 
        };

        if (!data.supplierName) {
            api.showToast("Partner name is required", "error");
            return;
        }

        try {
            if (id) {
                await api.put(`/admin/suppliers/${id}`, data);
                api.showToast("Partner updated successfully", "success");
            } else {
                await api.post('/admin/suppliers', data);
                api.showToast("Partner added successfully", "success");
            }
            
            bootstrap.Modal.getInstance(document.getElementById('supplierModal')).hide();
            suppliers.loadList();
            
        } catch (e) {
            console.error(e);
            api.showToast("Error saving partner", "error");
        }
    },

    delete: async (id) => {
        if (!confirm("Remove this supplier?")) return;
        try {
            await api.delete(`/admin/suppliers/${id}`);
            api.showToast("Supplier removed", "success");
            suppliers.loadList();
        } catch (e) { 
            api.showToast("Failed to remove supplier", "error"); 
        }
    }
};