use newgpt_rust_core::{cosine, surprise};
use std::time::Instant;

fn main() {
    let a: Vec<f32> = (0..384).map(|i| i as f32 / 384.0).collect();
    let b = a.clone();
    let warmup = 10_000;
    for _ in 0..warmup { std::hint::black_box(cosine(&a, &b)); }

    let samples = 100_000;
    let start = Instant::now();
    let mut acc = 0.0f32;
    for _ in 0..samples {
        let c = cosine(&a, &b);
        acc += surprise(c);
    }
    let elapsed = start.elapsed();
    let ns = elapsed.as_nanos() as f64 / samples as f64;
    println!("samples={samples} total_ms={:.3} avg_ns={:.1} avg_us={:.4} sink={acc}", elapsed.as_secs_f64()*1000.0, ns, ns/1000.0);
}