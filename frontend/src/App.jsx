import { useState } from 'react'
import './App.css'

/**
 * Componente principal de la aplicación de facturación.
 * Maneja el estado y la lógica de la interfaz de usuario.
 */
function App() {
  // Estado para información del cliente
  const [customer, setCustomer] = useState({
    name: '',
    identification: '',
    email: ''
  })

  // Estado para lista de productos
  const [products, setProducts] = useState([
    { name: '', price: '', quantity: '', total: 0 }
  ])

  // Estado para el PDF generado
  const [pdfUrl, setPdfUrl] = useState(null)

  // Estado para mensajes de error
  const [error, setError] = useState(null)

  // Estado de carga
  const [loading, setLoading] = useState(false)

  /**
   * Maneja cambios en los campos del cliente.
   * 
   * @param {Event} e - Evento del input
   */
  const handleCustomerChange = (e) => {
    const { name, value } = e.target
    setCustomer(prev => ({
      ...prev,
      [name]: value
    }))
  }

  /**
   * Maneja cambios en los campos de productos.
   * Calcula automáticamente el total por producto.
   * 
   * @param {number} index - Índice del producto en el array
   * @param {Event} e - Evento del input
   */
  const handleProductChange = (index, e) => {
    const { name, value } = e.target
    const newProducts = [...products]
    newProducts[index][name] = value

    // Calcular total automáticamente
    if (name === 'price' || name === 'quantity') {
      const price = parseFloat(newProducts[index].price) || 0
      const quantity = parseInt(newProducts[index].quantity) || 0
      newProducts[index].total = price * quantity
    }

    setProducts(newProducts)
  }

  /**
   * Agrega un nuevo producto vacío a la lista.
   */
  const addProduct = () => {
    setProducts([...products, { name: '', price: '', quantity: '', total: 0 }])
  }

  /**
   * Elimina un producto de la lista.
   * 
   * @param {number} index - Índice del producto a eliminar
   */
  const removeProduct = (index) => {
    if (products.length > 1) {
      const newProducts = products.filter((_, i) => i !== index)
      setProducts(newProducts)
    }
  }

  /**
   * Calcula el total general de todos los productos.
   * 
   * @returns {number} Total general
   */
  const calculateTotal = () => {
    return products.reduce((sum, product) => sum + (product.total || 0), 0)
  }

  /**
   * Valida el formato del email usando expresión regular.
   * 
   * @param {string} email - Email a validar
   * @returns {boolean} true si el email es válido
   */
  const validateEmail = (email) => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
    return emailRegex.test(email)
  }

  /**
   * Valida el formulario completo antes de enviar.
   * 
   * @returns {Object} { valid: boolean, message: string }
   */
  const validateForm = () => {
    // Validar cliente
    if (!customer.name.trim()) {
      return { valid: false, message: 'El nombre del cliente es obligatorio' }
    }
    if (!customer.identification.trim()) {
      return { valid: false, message: 'La identificación es obligatoria' }
    }
    if (!customer.email.trim()) {
      return { valid: false, message: 'El email es obligatorio' }
    }
    if (!validateEmail(customer.email)) {
      return { valid: false, message: 'El formato del email no es válido' }
    }

    // Validar productos
    if (products.length === 0) {
      return { valid: false, message: 'Debe agregar al menos un producto' }
    }

    for (let i = 0; i < products.length; i++) {
      const product = products[i]
      if (!product.name.trim()) {
        return { valid: false, message: `El producto ${i + 1} debe tener nombre` }
      }
      if (!product.price || parseFloat(product.price) <= 0) {
        return { valid: false, message: `El producto ${i + 1} debe tener un precio válido` }
      }
      if (!product.quantity || parseInt(product.quantity) <= 0) {
        return { valid: false, message: `El producto ${i + 1} debe tener una cantidad válida` }
      }
    }

    return { valid: true }
  }

  /**
   * Maneja el envío del formulario y la creación de la venta.
   * Envía los datos al backend y procesa la respuesta.
   */
  const handleSubmit = async () => {
    // Limpiar estados previos
    setError(null)
    setPdfUrl(null)

    // Validar formulario
    const validation = validateForm()
    if (!validation.valid) {
      setError(validation.message)
      return
    }

    setLoading(true)

    try {
      // Preparar datos para enviar
      const saleData = {
        customer: {
          name: customer.name.trim(),
          identification: customer.identification.trim(),
          email: customer.email.trim()
        },
        products: products.map(p => ({
          name: p.name.trim(),
          price: parseFloat(p.price),
          quantity: parseInt(p.quantity),
          total: parseFloat(p.total)
        }))
      }

      console.log('📤 Enviando venta al backend...', saleData)

      // Enviar petición al backend
      const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      const response = await fetch(`${API_URL}/api/sales`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': 'orchestrator-secret-key-123456789'
        },
        body: JSON.stringify(saleData)
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.message || `Error: ${response.status} ${response.statusText}`)
      }

      // Obtener PDF como blob
      const pdfBlob = await response.blob()
      const pdfObjectUrl = URL.createObjectURL(pdfBlob)

      console.log('✅ PDF recibido exitosamente')
      setPdfUrl(pdfObjectUrl)

    } catch (err) {
      console.error('❌ Error al procesar venta:', err)
      setError(`Error al procesar la venta: ${err.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>🧾 Sistema de Facturación Electrónica</h1>
        <p>Crea facturas y envíalas por email automáticamente</p>
      </header>

      <div className="container">
        {/* Panel Izquierdo - Formulario */}
        <div className="left-panel">
          <div className="form-section">
            <h2>📋 Datos del Cliente</h2>
            
            <div className="form-group">
              <label htmlFor="name">Nombre Completo *</label>
              <input
                type="text"
                id="name"
                name="name"
                value={customer.name}
                onChange={handleCustomerChange}
                placeholder="Juan Pérez"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="identification">Número de Identificación *</label>
              <input
                type="text"
                id="identification"
                name="identification"
                value={customer.identification}
                onChange={handleCustomerChange}
                placeholder="1234567890"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Correo Electrónico *</label>
              <input
                type="email"
                id="email"
                name="email"
                value={customer.email}
                onChange={handleCustomerChange}
                placeholder="correo@ejemplo.com"
                required
              />
              <small>Debe tener formato: usuario@dominio.ext</small>
            </div>
          </div>

          <div className="form-section">
            <h2>🛒 Productos</h2>
            
            {products.map((product, index) => (
              <div key={index} className="product-item">
                <div className="product-header">
                  <h3>Producto {index + 1}</h3>
                  {products.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeProduct(index)}
                      className="btn-remove"
                      title="Eliminar producto"
                    >
                      ✕
                    </button>
                  )}
                </div>

                <div className="form-group">
                  <label>Nombre del Producto *</label>
                  <input
                    type="text"
                    name="name"
                    value={product.name}
                    onChange={(e) => handleProductChange(index, e)}
                    placeholder="Nombre del producto"
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Precio (COP) *</label>
                    <input
                      type="number"
                      name="price"
                      value={product.price}
                      onChange={(e) => handleProductChange(index, e)}
                      placeholder="50000"
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Cantidad *</label>
                    <input
                      type="number"
                      name="quantity"
                      value={product.quantity}
                      onChange={(e) => handleProductChange(index, e)}
                      placeholder="1"
                      min="1"
                      required
                    />
                  </div>
                </div>

                <div className="product-total">
                  <strong>Total:</strong> ${product.total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                </div>
              </div>
            ))}

            <button
              type="button"
              onClick={addProduct}
              className="btn-add"
            >
              ➕ Agregar Producto
            </button>
          </div>

          <div className="total-section">
            <h2>💰 Total General</h2>
            <div className="total-amount">
              ${calculateTotal().toLocaleString('es-CO', { minimumFractionDigits: 2 })}
            </div>
          </div>

          <button
            onClick={handleSubmit}
            disabled={loading}
            className="btn-submit"
          >
            {loading ? '⏳ Procesando...' : '🚀 Realizar Venta'}
          </button>

          {error && (
            <div className="error-message">
              ⚠️ {error}
            </div>
          )}
        </div>

        {/* Panel Derecho - Visualización del PDF */}
        <div className="right-panel">
          <h2>📄 Factura Generada</h2>
          
          {!pdfUrl && !error && !loading && (
            <div className="placeholder">
              <p>📝 La factura aparecerá aquí una vez realizada la venta.</p>
              <p>Complete el formulario y haga clic en "Realizar Venta".</p>
            </div>
          )}

          {loading && (
            <div className="placeholder">
              <div className="loader"></div>
              <p>Generando factura...</p>
            </div>
          )}

          {pdfUrl && !loading && (
            <div className="pdf-container">
              <iframe
                src={pdfUrl}
                title="Factura PDF"
                width="100%"
                height="100%"
              />
              <a
                href={pdfUrl}
                download="factura.pdf"
                className="btn-download"
              >
                ⬇️ Descargar PDF
              </a>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default App