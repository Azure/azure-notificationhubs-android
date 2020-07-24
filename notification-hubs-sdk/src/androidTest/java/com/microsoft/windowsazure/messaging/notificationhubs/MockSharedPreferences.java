package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * An in-memory implementation of {@link SharedPreferences} to allow for the creation of UnitTests.
 */
public class MockSharedPreferences implements SharedPreferences {
    private final Map<String, Object> mValues = new HashMap<String, Object>();
    private final Set<OnSharedPreferenceChangeListener> mListeners = new HashSet<>();
    private final SharedPreferences.Editor mEditor = new Editor();

    /**
     *
     * @param key
     * @param defValue
     * @param <T>
     * @return
     *
     */
    private <T> T getIfPresent(String key, T defValue){
        synchronized (mValues) {
            if (mValues.containsKey(key)) {
                return (T) mValues.get(key);
            }
            return defValue;
        }
    }

    /**
     * Retrieve all values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the collection returned
     * by this method, or alter any of its contents.  The consistency of your
     * stored data is not guaranteed if you do.
     *
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     * @throws NullPointerException
     */
    @Override
    public Map<String, ?> getAll() {
        synchronized (mValues) {
            Map<String, Object> retVal = new HashMap<>();
            for (Map.Entry<String, Object> entry : mValues.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof Set) {
                    retVal.put(entry.getKey(), new HashSet<String>((Set<String>) val));
                } else {
                    retVal.put(entry.getKey(), val);
                }
            }

            return retVal;
        }
    }

    /**
     * Retrieve a String value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     * @throws ClassCastException
     */
    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return getIfPresent(key, defValue);
    }

    /**
     * Retrieve a set of String values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the set instance returned
     * by this call.  The consistency of the stored data is not guaranteed
     * if you do, nor is your ability to modify the instance at all.
     *
     * @param key       The name of the preference to retrieve.
     * @param defValues Values to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this name
     * that is not a Set.
     * @throws ClassCastException
     */
    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return new HashSet<>(getIfPresent(key, defValues));
    }

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * an int.
     * @throws ClassCastException
     */
    @Override
    public int getInt(String key, int defValue) {
        return getIfPresent(key, defValue);
    }

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a long.
     * @throws ClassCastException
     */
    @Override
    public long getLong(String key, long defValue) {
        return getIfPresent(key, defValue);
    }

    /**
     * Retrieve a float value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a float.
     * @throws ClassCastException
     */
    @Override
    public float getFloat(String key, float defValue) {
        return getIfPresent(key, defValue);
    }

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a boolean.
     * @throws ClassCastException
     */
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return getIfPresent(key, defValue);
    }

    /**
     * Checks whether the preferences contains a preference.
     *
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the preferences,
     * otherwise false.
     */
    @Override
    public boolean contains(String key) {
        return mValues.containsKey(key);
    }

    /**
     * Create a new Editor for these preferences, through which you can make
     * modifications to the data in the preferences and atomically commit those
     * changes back to the SharedPreferences object.
     *
     * <p>Note that you <em>must</em> call {@link Editor#commit} to have any
     * changes you perform in the Editor actually show up in the
     * SharedPreferences.
     *
     * @return Returns a new instance of the {@link Editor} interface, allowing
     * you to modify the values in this SharedPreferences object.
     */
    @Override
    public SharedPreferences.Editor edit() {
        return mEditor;
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * <p class="caution"><strong>Caution:</strong> The preference manager does
     * not currently store a strong reference to the listener. You must store a
     * strong reference to the listener, or it will be susceptible to garbage
     * collection. We recommend you keep a reference to the listener in the
     * instance data of an object that will exist as long as you need the
     * listener.</p>
     *
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public class Editor implements SharedPreferences.Editor {
        private class Edit<T> {
            private String mKey;
            private T mValue;

            public Edit(String key, T value) {
                mKey = key;
                mValue = value;
            }

            public String getKey() {
                return mKey;
            }

            public T getValue() {
                return mValue;
            }
        }
        private final ArrayList<Edit<?>> mPendingChanges = new ArrayList<>();
        private boolean mShouldClear;

        /**
         * Set a String value in the preferences editor, to be written back once
         * {@link #commit} or {@link #apply} are called.
         *
         * @param key   The name of the preference to modify.
         * @param value The new value for the preference.  Passing {@code null}
         *              for this argument is equivalent to calling {@link #remove(String)} with
         *              this key.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            return put(key, value);
        }

        /**
         * Set a set of String values in the preferences editor, to be written
         * back once {@link #commit} or {@link #apply} is called.
         *
         * @param key    The name of the preference to modify.
         * @param values The set of new values for the preference.  Passing {@code null}
         *               for this argument is equivalent to calling {@link #remove(String)} with
         *               this key.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            return put(key, values);
        }

        /**
         * Set an int value in the preferences editor, to be written back once
         * {@link #commit} or {@link #apply} are called.
         *
         * @param key   The name of the preference to modify.
         * @param value The new value for the preference.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            return put(key, value);
        }

        /**
         * Set a long value in the preferences editor, to be written back once
         * {@link #commit} or {@link #apply} are called.
         *
         * @param key   The name of the preference to modify.
         * @param value The new value for the preference.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            return put(key, value);
        }

        /**
         * Set a float value in the preferences editor, to be written back once
         * {@link #commit} or {@link #apply} are called.
         *
         * @param key   The name of the preference to modify.
         * @param value The new value for the preference.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            return put(key, value);
        }

        /**
         * Set a boolean value in the preferences editor, to be written back
         * once {@link #commit} or {@link #apply} are called.
         *
         * @param key   The name of the preference to modify.
         * @param value The new value for the preference.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            return put(key, value);
        }

        private <T> SharedPreferences.Editor put(String key, T value) {
            synchronized (mPendingChanges) {
                mPendingChanges.add(new Edit<>(key, value));
            }
            return this;
        }

        /**
         * Mark in the editor that a preference value should be removed, which
         * will be done in the actual preferences once {@link #commit} is
         * called.
         *
         * <p>Note that when committing back to the preferences, all removals
         * are done first, regardless of whether you called remove before
         * or after put methods on this editor.
         *
         * @param key The name of the preference to remove.
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor remove(String key) {
            return put(key, null);
        }

        /**
         * Mark in the editor to remove <em>all</em> values from the
         * preferences.  Once commit is called, the only remaining preferences
         * will be any that you have defined in this editor.
         *
         * <p>Note that when committing back to the preferences, the clear
         * is done first, regardless of whether you called clear before
         * or after put methods on this editor.
         *
         * @return Returns a reference to the same Editor object, so you can
         * chain put calls together.
         */
        @Override
        public SharedPreferences.Editor clear() {
            synchronized (mPendingChanges) {
                mShouldClear = true;
            }
            return this;
        }

        /**
         * Commit your preferences changes back from this Editor to the
         * {@link SharedPreferences} object it is editing.  This atomically
         * performs the requested modifications, replacing whatever is currently
         * in the SharedPreferences.
         *
         * <p>Note that when two editors are modifying preferences at the same
         * time, the last one to call commit wins.
         *
         * <p>If you don't care about the return value and you're
         * using this from your application's main thread, consider
         * using {@link #apply} instead.
         *
         * @return Returns true if the new values were successfully written
         * to persistent storage.
         */
        @Override
        public boolean commit() {
            synchronized (mPendingChanges) {
                synchronized (mValues) {
                    synchronized (mListeners) {
                        if (mShouldClear) {
                            mValues.clear();
                        }

                        for (Edit<?> edit: mPendingChanges) {
                            Object proposedValue = edit.getValue();
                            if (proposedValue == null) {
                                mValues.remove(edit.getKey());
                            } else {
                                mValues.put(edit.getKey(), proposedValue);
                            }

                            notifyListeners(edit);
                        }
                    }
                }
                mShouldClear = false;
            }
            return true;
        }

        private void notifyListeners(Edit<?> edit) {
            for (OnSharedPreferenceChangeListener listener : mListeners) {
                listener.onSharedPreferenceChanged(MockSharedPreferences.this, edit.getKey());
            }
        }

        /**
         * Commit your preferences changes back from this Editor to the
         * {@link SharedPreferences} object it is editing.  This atomically
         * performs the requested modifications, replacing whatever is currently
         * in the SharedPreferences.
         *
         * <p>Note that when two editors are modifying preferences at the same
         * time, the last one to call apply wins.
         *
         * <p>Unlike {@link #commit}, which writes its preferences out
         * to persistent storage synchronously, {@link #apply}
         * commits its changes to the in-memory
         * {@link SharedPreferences} immediately but starts an
         * asynchronous commit to disk and you won't be notified of
         * any failures.  If another editor on this
         * {@link SharedPreferences} does a regular {@link #commit}
         * while a {@link #apply} is still outstanding, the
         * {@link #commit} will block until all async commits are
         * completed as well as the commit itself.
         *
         * <p>As {@link SharedPreferences} instances are singletons within
         * a process, it's safe to replace any instance of {@link #commit} with
         * {@link #apply} if you were already ignoring the return value.
         *
         * <p>You don't need to worry about Android component
         * lifecycles and their interaction with <code>apply()</code>
         * writing to disk.  The framework makes sure in-flight disk
         * writes from <code>apply()</code> complete before switching
         * states.
         *
         * <p class='note'>The SharedPreferences.Editor interface
         * isn't expected to be implemented directly.  However, if you
         * previously did implement it and are now getting errors
         * about missing <code>apply()</code>, you can simply call
         * {@link #commit} from <code>apply()</code>.
         */
        @Override
        public void apply() {
            commit();
        }
    }
}
