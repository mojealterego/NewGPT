#![allow(clippy::missing_safety_doc)]

use std::slice;

#[inline]
pub fn cosine(a: &[f32], b: &[f32]) -> f32 {
    if a.is_empty() || a.len() != b.len() { return 0.0; }
    let mut dot = 0.0f32;
    let mut aa = 0.0f32;
    let mut bb = 0.0f32;
    for i in 0..a.len() {
        dot += a[i] * b[i];
        aa += a[i] * a[i];
        bb += b[i] * b[i];
    }
    if aa <= f32::EPSILON || bb <= f32::EPSILON { return 0.0; }
    (dot / (aa.sqrt() * bb.sqrt())).clamp(-1.0, 1.0)
}

#[inline]
pub fn surprise(similarity: f32) -> f32 {
    (1.0 - ((similarity + 1.0) * 0.5)).clamp(0.0, 1.0)
}

#[no_mangle]
pub unsafe extern "C" fn newgpt_cosine(
    a_ptr: *const f32,
    a_len: usize,
    b_ptr: *const f32,
    b_len: usize,
) -> f32 {
    if a_ptr.is_null() || b_ptr.is_null() || a_len != b_len { return 0.0; }
    let a = slice::from_raw_parts(a_ptr, a_len);
    let b = slice::from_raw_parts(b_ptr, b_len);
    cosine(a, b)
}

#[no_mangle]
pub unsafe extern "C" fn newgpt_surprise(similarity: f32) -> f32 {
    surprise(similarity)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn cosine_identical_is_one() {
        let v = [1.0, 0.0, 0.0];
        assert!((cosine(&v, &v) - 1.0).abs() < 1e-6);
    }

    #[test]
    fn surprise_identical_is_zero() {
        assert!(surprise(1.0).abs() < 1e-6);
    }
}
