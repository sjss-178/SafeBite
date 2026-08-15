import { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { ChefHat, ShieldCheck, ShieldAlert, AlertCircle, Loader2, Sparkles } from 'lucide-react';

const API_BASE = 'http://localhost:8081/api/safebite';

function App() {
  const [menu, setMenu] = useState([]);
  const [allergens, setAllergens] = useState([]);
  const [selectedAllergens, setSelectedAllergens] = useState([]);
  const [unsafeItems, setUnsafeItems] = useState([]);
  
  // New state for the Recommendation Engine
  const [selectedCategory, setSelectedCategory] = useState('');
  const [safeRecommendations, setSafeRecommendations] = useState([]);
  const [loadingRecs, setLoadingRecs] = useState(false);
  
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState(null);

  // Derive unique categories dynamically from the menu
  const categories = useMemo(() => {
    return [...new Set(menu.map(item => item.category))].sort();
  }, [menu]);

  // 1. Fetch initial data on load
  useEffect(() => {
    const fetchBaseData = async () => {
      try {
        const [menuRes, allergensRes] = await Promise.all([
          axios.get(`${API_BASE}/menu`),
          axios.get(`${API_BASE}/allergens`)
        ]);
        setMenu(menuRes.data);
        setAllergens(allergensRes.data);
        setError(null);
      } catch (err) {
        setError(err.response?.data?.message || 'Cannot connect to the SafeBite database.');
      } finally {
        setLoadingInitial(false);
      }
    };
    fetchBaseData();
  }, []);

  // 2. Trigger Graph Traversal for UNSAFE items
  useEffect(() => {
    if (selectedAllergens.length === 0) {
      setUnsafeItems([]);
      return;
    }
    const analyzeSafety = async () => {
      setAnalyzing(true);
      try {
        const res = await axios.get(`${API_BASE}/unsafe?allergens=${selectedAllergens.join(',')}`);
        setUnsafeItems(res.data);
      } catch (err) {
        console.error('Failed to analyze menu safety.');
      } finally {
        setAnalyzing(false);
      }
    };
    const delay = setTimeout(() => analyzeSafety(), 300);
    return () => clearTimeout(delay);
  }, [selectedAllergens]);

  // 3. NEW: Trigger Graph Traversal for SAFE RECOMMENDATIONS
  useEffect(() => {
    if (!selectedCategory) {
      setSafeRecommendations([]);
      return;
    }
    const fetchRecommendations = async () => {
      setLoadingRecs(true);
      try {
        const queryParams = selectedAllergens.length > 0 ? `&allergens=${selectedAllergens.join(',')}` : '';
        const res = await axios.get(`${API_BASE}/safe-alternatives?category=${encodeURIComponent(selectedCategory)}${queryParams}`);
        setSafeRecommendations(res.data);
      } catch (err) {
        console.error('Failed to fetch recommendations.');
      } finally {
        setLoadingRecs(false);
      }
    };
    const delay = setTimeout(() => fetchRecommendations(), 300);
    return () => clearTimeout(delay);
  }, [selectedCategory, selectedAllergens]);

  const toggleAllergen = (allergenName) => {
    setSelectedAllergens(prev => 
      prev.includes(allergenName) 
        ? prev.filter(a => a !== allergenName)
        : [...prev, allergenName]
    );
  };

  if (error && menu.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-red-100 max-w-md text-center">
          <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-slate-800 mb-2">System Offline</h2>
          <p className="text-slate-600">{error}</p>
        </div>
      </div>
    );
  }

  if (loadingInitial) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50">
        <Loader2 className="w-10 h-10 text-emerald-600 animate-spin mb-4" />
        <p className="text-slate-500 font-medium tracking-wide animate-pulse">Loading SafeBite Menu...</p>
      </div>
    );
  }
  // 4. Reset everything to default state
  const handleReset = () => {
    setSelectedAllergens([]);
    setSelectedCategory('');
    setUnsafeItems([]);
    setSafeRecommendations([]);
  };

  return (
    <div className="min-h-screen font-sans text-slate-800 flex flex-col bg-slate-50">
      <header className="bg-white border-b border-slate-200 px-8 py-4 flex items-center justify-between sticky top-0 z-30 shadow-sm">
        
        {/* Left Side: Branding */}
        <div className="flex items-center gap-3">
          <div className="bg-emerald-100 p-2 rounded-lg shadow-inner">
            <ChefHat className="w-6 h-6 text-emerald-700" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 leading-tight tracking-tight">SafeBite</h1>
            <p className="text-xs text-slate-500 font-bold uppercase tracking-widest">Deep Supply Chain Tracer</p>
          </div>
        </div>

        {/* Right Side: Controls, Status & Profile */}
        <div className="flex items-center gap-3 sm:gap-5">
          
          {/* 1. The Reset Button */}
          <button 
            onClick={handleReset}
            disabled={selectedAllergens.length === 0 && !selectedCategory}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-bold rounded-xl border transition-all duration-200 
              ${selectedAllergens.length === 0 && !selectedCategory 
                ? 'bg-slate-50 text-slate-400 border-slate-200 cursor-not-allowed opacity-50' 
                : 'bg-white text-rose-600 border-rose-200 hover:bg-rose-50 hover:border-rose-300 hover:shadow-sm active:scale-95'
              }`}
          >
            
            <span className="hidden sm:inline">Reset Filters</span>
          </button>

          {/* 2. System Status Indicator */}
          <div className="hidden md:flex items-center gap-2 px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-full shadow-sm">
            <div className="relative flex h-2.5 w-2.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
            </div>
            <span className="text-xs font-bold text-slate-600 uppercase tracking-wider">CognoDB Connected</span>
          </div>

          {/* Vertical Divider */}
          <div className="w-px h-8 bg-slate-200 hidden lg:block"></div>

          {/* 3. User Profile Mockup */}
          <button className="hidden sm:flex items-center gap-3 hover:bg-slate-50 p-1.5 rounded-xl transition-colors text-left">
            <div className="hidden lg:block">
              <p className="text-sm font-bold text-slate-800 leading-none">Alex Auditor</p>
              <p className="text-xs text-slate-500 font-medium mt-1">Supply Chain Admin</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-emerald-700 flex items-center justify-center text-white font-bold border-2 border-emerald-100 shadow-sm">
              AA
            </div>
          </button>
          
        </div>
      </header>

      <div className="flex-1 flex flex-col md:flex-row max-w-7xl mx-auto w-full px-4 py-8 gap-8">
        
        {/* LEFT SIDEBAR */}
        <aside className="w-full md:w-80 flex-shrink-0 flex flex-col gap-6">
          {/* Dietary Profile */}
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 sticky top-28">
            <h2 className="text-lg font-bold mb-1">Dietary Profile</h2>
            <p className="text-sm text-slate-500 mb-6">Select allergens to trace cross-contamination up to 4 levels deep.</p>
            
            <div className="space-y-3">
              {allergens.map(allergen => (
                <label key={allergen.name} className={`flex items-center p-3 rounded-xl border cursor-pointer transition-all ${selectedAllergens.includes(allergen.name) ? 'bg-emerald-50 border-emerald-200' : 'bg-white border-slate-200 hover:bg-slate-50'}`}>
                  <input type="checkbox" className="w-4 h-4 text-emerald-600 rounded border-gray-300" checked={selectedAllergens.includes(allergen.name)} onChange={() => toggleAllergen(allergen.name)} />
                  <span className="ml-3 text-sm font-semibold text-slate-700">{allergen.name}</span>
                </label>
              ))}
            </div>
          </div>

          {/* NEW: Recommendation Engine UI */}
          <div className="bg-slate-900 text-white rounded-2xl p-6 shadow-md sticky top-[28rem]">
            <h2 className="text-lg font-bold mb-1 flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-yellow-400" />
              Smart Recommendations
            </h2>
            <p className="text-sm text-slate-400 mb-4">Select a category to find 100% safe alternatives for your profile.</p>
            <select 
              className="w-full p-3 rounded-xl bg-slate-800 border border-slate-700 text-white focus:ring-2 focus:ring-emerald-500 outline-none"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="">-- Select a Category --</option>
              {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
            </select>
          </div>
        </aside>

        {/* RIGHT PANEL */}
        <main className="flex-1">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold">Our Menu</h2>
            {analyzing && (
              <div className="flex items-center gap-2 text-sm font-medium text-emerald-600 bg-emerald-50 px-3 py-1.5 rounded-full">
                <Loader2 className="w-4 h-4 animate-spin" /> Tracing supply chain...
              </div>
            )}
          </div>

          {/* NEW: Safe Recommendations Highlight Box */}
          {selectedCategory && (
            <div className="mb-8 bg-emerald-50 border border-emerald-200 rounded-2xl p-6 shadow-sm">
              <h3 className="text-lg font-bold text-emerald-900 mb-4 flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-600" />
                Safe {selectedCategory} Options
              </h3>
              
              {loadingRecs ? (
                <div className="flex items-center gap-2 text-emerald-700 text-sm"><Loader2 className="w-4 h-4 animate-spin" /> Analyzing graph paths...</div>
              ) : safeRecommendations.length === 0 ? (
                <p className="text-sm text-rose-600 font-medium">Unfortunately, no items in this category are completely safe for your profile.</p>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {safeRecommendations.map(item => (
                    <div key={item.safeAlternative} className="bg-white p-3 rounded-xl border border-emerald-100 flex justify-between items-center shadow-sm">
                      <span className="font-bold text-slate-800">{item.safeAlternative}</span>
                      <span className="text-emerald-700 font-bold">${item.price.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Main Menu Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {menu.map(item => {
              const unsafeData = unsafeItems.find(u => u.unsafeMenuItem === item.name);
              const isUnsafe = !!unsafeData;

              return (
                <div key={item.name} className={`relative p-6 rounded-2xl border transition-all duration-300 ${isUnsafe ? 'bg-rose-50 border-rose-200 opacity-90' : 'bg-white border-slate-200 hover:shadow-md'}`}>
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className={`text-lg font-bold ${isUnsafe ? 'text-rose-900' : 'text-slate-900'}`}>{item.name}</h3>
                      <p className="text-sm font-medium text-slate-500">{item.category}</p>
                    </div>
                    <span className="text-lg font-bold text-slate-700">${item.price.toFixed(2)}</span>
                  </div>

                  {selectedAllergens.length > 0 && (
                    <div className="mt-4 pt-4 border-t border-slate-200/60">
                      {isUnsafe ? (
                        <div className="flex items-start gap-2 text-rose-700">
                          <ShieldAlert className="w-5 h-5 flex-shrink-0" />
                          <div>
                            <p className="text-sm font-bold">Unsafe: Contains {unsafeData.triggeredAllergen}</p>
                            <p className="text-xs font-medium opacity-80 mt-0.5">Detected {unsafeData.depthHops} {unsafeData.depthHops === 1 ? 'level' : 'levels'} deep in supply chain.</p>
                          </div>
                        </div>
                      ) : (
                        <div className="flex items-center gap-2 text-emerald-600">
                          <ShieldCheck className="w-5 h-5" />
                          <p className="text-sm font-bold">Safe for your profile</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </main>
      </div>
    </div>
  );
}

export default App;