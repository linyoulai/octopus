package cn.throwx.octopus.server.filter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author throwable
 * @description
 * @since 2020/7/21 16:25
 */
@Slf4j
public class DefaultTransformFilterChain implements TransformFilterChain {

    private int pos = 0; // 当前过滤器位置
    private int n = 0; // 数组大小
    private TransformFilter[] filters = new TransformFilter[0]; // 空的过滤器数组

    @Override
    public void doFilter(TransformContext context) { // 过滤
        if (this.pos < this.n) { // 还没过滤完毕
            TransformFilter transformFilter = this.filters[this.pos++]; // 获取当前过滤器对象
            transformFilter.doFilter(this, context); // 过滤器开始过滤
        }
    }

    // 为过滤器数组添加过滤器
    void addTransformFilter(TransformFilter filter) {
        TransformFilter[] newFilters = this.filters;
        for (TransformFilter newFilter : newFilters) {
            if (newFilter == filter) { // 如果过滤器已经存在，则不添加
                return;
            }
        }
        // 扩容
        if (this.n == this.filters.length) {
            newFilters = new TransformFilter[this.n + 10];
            System.arraycopy(this.filters, 0, newFilters, 0, this.n);
            this.filters = newFilters;
        }
        this.filters[this.n++] = filter;
    }

    @Override
    public void release() { // ？
        for (int i = 0; i < this.n; ++i) {
            this.filters[i] = null;
        }
        this.pos = 0;
        this.n = 0;
    }
}
